package com.xtremeiptv.service.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.DownloadNotificationHelper
import com.xtremeiptv.feature.player.PlayerActivity
import java.io.File
import java.util.concurrent.Executor

@UnstableApi
class DownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download_channel_name
) {

    override fun getDownloadManager(): DownloadManager {
        return downloadManager ?: run {
            val downloadCacheDir = File(applicationContext.filesDir, "downloads")
            val downloadCache = SimpleCache(
                downloadCacheDir,
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
            )

            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("XtremeIPTV")

            DownloadManager(
                applicationContext,
                downloadCache,
                CacheDataSource.Factory()
                    .setCache(downloadCache)
                    .setUpstreamDataSourceFactory(dataSourceFactory)
                    .setCacheWriteDataSinkFactory(null),
                dataSourceFactory,
                Executor { command -> command.run() }
            ).apply {
                maxParallelDownloads = 3
                minRetryCount = 3
                requirements = DownloadManager.Requirements.Builder(applicationContext)
                    .setRequiredNetworkType(DownloadManager.Requirements.NETWORK_TYPE_ANY)
                    .setRequiredStorageNotLow(true)
                    .build()
                setScheduler(PlatformScheduler(applicationContext, JOB_ID))
            }.also { downloadManager = it }
        }
    }

    override fun getScheduler(): PlatformScheduler? {
        return PlatformScheduler(this, JOB_ID)
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        return buildNotification(downloads)
    }

    private fun buildNotification(downloads: MutableList<Download>): Notification {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.download_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.download_channel_description)
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationHelper = DownloadNotificationHelper(applicationContext, channelId)

        return when (val state = DownloadManager.getDownloadState(downloads)) {
            DownloadManager.STATE_DOWNLOADING -> {
                val totalProgress = DownloadManager.getTotalProgress(downloads).toInt()
                notificationHelper.buildProgressNotification(
                    this,
                    R.drawable.ic_download,
                    null,
                    getString(R.string.download_progress, totalProgress),
                    totalProgress.toString()
                )
            }
            DownloadManager.STATE_COMPLETED -> {
                val intent = Intent(this, PlayerActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                NotificationCompat.Builder(this, channelId)
                    .setContentTitle(getString(R.string.download_complete))
                    .setContentText(getString(R.string.download_complete_message))
                    .setSmallIcon(R.drawable.ic_download_done)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
            }
            else -> notificationHelper.buildDownloadingNotification(
                this,
                R.drawable.ic_download,
                null,
                getString(R.string.download_pending)
            )
        }
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val JOB_ID = 1
        private const val CHANNEL_ID = "download_channel"
        private const val MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100 MB

        @Volatile
        private var downloadManager: DownloadManager? = null
    }
}
