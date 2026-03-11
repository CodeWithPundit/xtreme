package com.xtremeiptv.feature.download.manager

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.cache.CacheDataSource
import androidx.media3.exoplayer.upstream.cache.NoOpCacheEvictor
import androidx.media3.exoplayer.upstream.cache.SimpleCache
import androidx.media3.ui.DownloadNotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadCache: SimpleCache,
    private val notificationHelper: DownloadNotificationHelper
) {
    
    private var downloadManager: DownloadManager? = null
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    
    private val downloadListener = object : DownloadManager.Listener {
        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download) {
            updateDownloadState(download)
            
            when (download.state) {
                Download.STATE_COMPLETED -> {
                    showNotification(download.request.id, "Download completed")
                }
                Download.STATE_FAILED -> {
                    showNotification(download.request.id, "Download failed: ${download.failureReason}")
                }
            }
        }
        
        override fun onDownloadsChanged(downloadManager: DownloadManager, downloads: MutableList<Download>) {
            _downloads.value = downloads.map { it.toDownloadItem() }
        }
    }
    
    fun initialize() {
        if (downloadManager == null) {
            val downloadCacheDir = File(context.filesDir, "downloads")
            
            downloadManager = DownloadManager(
                context,
                downloadCache,
                CacheDataSource.Factory()
                    .setCache(downloadCache)
                    .setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory()
                            .setUserAgent("XtremeIPTV")
                    )
                    .setCacheWriteDataSinkFactory(null),
                DefaultHttpDataSource.Factory()
                    .setUserAgent("XtremeIPTV")
            ).apply {
                addListener(downloadListener)
                maxParallelDownloads = 3
                minRetryCount = 3
                requirements = DownloadManager.Requirements.Builder(context)
                    .setRequiredNetworkType(DownloadManager.Requirements.NETWORK_TYPE_ANY)
                    .setRequiredStorageNotLow(true)
                    .build()
                setScheduler(PlatformScheduler(context, JobIntentService::class.java))
            }
        }
    }
    
    suspend fun startDownload(
        streamId: String,
        url: String,
        title: String,
        mimeType: String,
        quality: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val downloadHelper = createDownloadHelper(url, mimeType, quality)
            
            val downloadRequest = DownloadRequest.Builder(streamId, Uri.parse(url))
                .setData(title.toByteArray())
                .setStreamKeys(downloadHelper.getDownloadStreamKeys(downloadHelper.getMappedTrackInfo(0)))
                .build()
            
            downloadManager?.addDownload(downloadRequest) ?: throw Exception("Download manager not initialized")
            
            streamId
        }
    }
    
    fun pauseDownload(downloadId: String) {
        downloadManager?.pauseDownload(downloadId)
    }
    
    fun resumeDownload(downloadId: String) {
        downloadManager?.resumeDownload(downloadId)
    }
    
    fun removeDownload(downloadId: String) {
        downloadManager?.removeDownload(downloadId)
        downloadCache.removeResource(downloadId, CacheDataSource.DOWNLOAD_RANGE)
    }
    
    fun getDownloadState(downloadId: String): DownloadState? {
        return downloadManager?.currentDownloads
            ?.find { it.request.id == downloadId }
            ?.let { DownloadState.fromDownload(it) }
    }
    
    fun getDownloadPath(downloadId: String): String? {
        val cacheSpan = downloadCache.getCachedSpans(downloadId).firstOrNull()
        return cacheSpan?.file?.absolutePath
    }
    
    private fun createDownloadHelper(
        url: String,
        mimeType: String,
        quality: String?
    ): DownloadHelper {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("XtremeIPTV")
        
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        
        val trackSelector = DefaultTrackSelector(context)
        
        return DownloadHelper.forMediaItem(
            MediaItem.fromUri(Uri.parse(url)),
            DownloadHelper.getDownloadAction(
                when {
                    mimeType.contains("m3u8") -> C.CONTENT_TYPE_HLS
                    mimeType.contains("mpd") -> C.CONTENT_TYPE_DASH
                    else -> C.CONTENT_TYPE_OTHER
                }
            ),
            renderersFactory,
            dataSourceFactory,
            { trackSelector }
        ).apply {
            prepare { prepare() }
            runBlocking { getDownloadHelperResult(this@apply, quality) }
        }
    }
    
    private suspend fun getDownloadHelperResult(
        downloadHelper: DownloadHelper,
        quality: String?
    ) = suspendCoroutine<Unit> { continuation ->
        downloadHelper.addListener(object : DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                val trackGroups = downloadHelper.getMappedTrackInfo(0).getTrackGroups(0, 0)
                
                if (quality != null) {
                    selectQuality(downloadHelper, trackGroups, quality)
                }
                
                continuation.resume(Unit)
            }
            
            override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
    
    private fun selectQuality(
        downloadHelper: DownloadHelper,
        trackGroups: List<Format>,
        quality: String
    ) {
        val selectedIndex = when (quality) {
            "highest" -> trackGroups.indices.lastOrNull()
            "lowest" -> 0
            else -> {
                val bitrate = quality.toIntOrNull()
                trackGroups.indexOfFirst { format ->
                    format.bitrate == bitrate
                }.takeIf { it >= 0 }
            }
        }
        
        selectedIndex?.let { index ->
            val override = TrackSelectionOverride(
                trackGroups[index],
                C.INDEX_UNSET
            )
            downloadHelper.setTrackSelectionParameters(
                downloadHelper.trackSelectionParameters
                    .buildUpon()
                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                    .addOverride(override)
                    .build()
            )
        }
    }
    
    private fun updateDownloadState(download: Download) {
        val currentStates = _downloadStates.value.toMutableMap()
        currentStates[download.request.id] = DownloadState.fromDownload(download)
        _downloadStates.value = currentStates
    }
    
    private fun showNotification(downloadId: String, message: String) {
        val notification = notificationHelper.buildProgressNotification(
            context,
            Download.STATE_COMPLETED,
            null,
            1f
        )
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(downloadId.hashCode(), notification)
    }
    
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()
}

data class DownloadItem(
    val id: String,
    val title: String,
    val url: String,
    val state: DownloadState,
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val startTime: Long,
    val completionTime: Long? = null
)

enum class DownloadState {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    PAUSED,
    FAILED,
    REMOVED;
    
    companion object {
        fun fromDownload(download: Download): DownloadState {
            return when (download.state) {
                Download.STATE_QUEUED -> QUEUED
                Download.STATE_DOWNLOADING -> DOWNLOADING
                Download.STATE_COMPLETED -> COMPLETED
                Download.STATE_PAUSED -> PAUSED
                Download.STATE_FAILED -> FAILED
                Download.STATE_REMOVING -> REMOVED
                else -> FAILED
            }
        }
    }
}

fun Download.toDownloadItem(): DownloadItem {
    return DownloadItem(
        id = request.id,
        title = String(request.data ?: byteArrayOf()),
        url = request.uri.toString(),
        state = DownloadState.fromDownload(this),
        progress = getDownloadPercentage(this),
        downloadedBytes = getDownloadedBytes(this),
        totalBytes = contentLength,
        startTime = startTimeMs,
        completionTime = if (state == Download.STATE_COMPLETED) System.currentTimeMillis() else null
    )
}

private fun getDownloadPercentage(download: Download): Float {
    return if (download.contentLength > 0) {
        (download.getDownloadedBytes().toFloat() / download.contentLength.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
}

private fun getDownloadedBytes(download: Download): Long {
    return when (download.state) {
        Download.STATE_COMPLETED -> download.contentLength
        else -> download.getDownloadedBytes()
    }
}
