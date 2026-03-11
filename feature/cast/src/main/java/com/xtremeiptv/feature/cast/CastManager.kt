package com.xtremeiptv.feature.cast

import android.content.Context
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.PendingResult
import com.xtremeiptv.core.domain.model.StreamModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastManager @Inject constructor(
    private val context: Context
) {

    private var castContext: CastContext? = null
    private var currentSession: CastSession? = null
    private var remoteMediaClient: RemoteMediaClient? = null
    private var listeners = mutableListOf<CastListener>()

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession?) {}

        override fun onSessionStarted(session: CastSession?, sessionId: String) {
            currentSession = session
            remoteMediaClient = session?.remoteMediaClient
            listeners.forEach { it.onCastConnected() }
        }

        override fun onSessionStartFailed(session: CastSession?, error: Int) {
            listeners.forEach { it.onCastError("Failed to start session: $error") }
        }

        override fun onSessionEnding(session: CastSession?) {}

        override fun onSessionEnded(session: CastSession?, error: Int) {
            currentSession = null
            remoteMediaClient = null
            listeners.forEach { it.onCastDisconnected() }
        }

        override fun onSessionResuming(session: CastSession?, sessionId: String) {}

        override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
            currentSession = session
            remoteMediaClient = session?.remoteMediaClient
            listeners.forEach { it.onCastConnected() }
        }

        override fun onSessionResumeFailed(session: CastSession?, error: Int) {}

        override fun onSessionSuspended(session: CastSession?, reason: Int) {}
    }

    fun initialize() {
        castContext = CastContext.getSharedInstance(context)
        castContext?.sessionManager?.addSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
    }

    fun isCastAvailable(): Boolean {
        return castContext?.sessionManager?.currentCastSession != null
    }

    fun getCastSession(): CastSession? {
        return castContext?.sessionManager?.currentCastSession
    }

    fun playStream(stream: StreamModel, position: Long = 0) {
        val mediaInfo = createMediaInfo(stream)
        val mediaLoadOptions = MediaLoadOptions.Builder()
            .setAutoplay(true)
            .setPlayPosition(position)
            .build()

        remoteMediaClient?.load(mediaInfo, mediaLoadOptions)
    }

    fun pausePlayback() {
        remoteMediaClient?.pause()
    }

    fun resumePlayback() {
        remoteMediaClient?.play()
    }

    fun seekTo(position: Long) {
        remoteMediaClient?.seek(position)
    }

    fun stopPlayback() {
        remoteMediaClient?.stop()
    }

    fun getPlaybackState(): Int {
        return remoteMediaClient?.playbackState ?: -1
    }

    fun getCurrentPosition(): Long {
        return remoteMediaClient?.approximateStreamPosition ?: 0
    }

    fun getStreamDuration(): Long {
        return remoteMediaClient?.streamDuration ?: 0
    }

    fun addListener(listener: CastListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: CastListener) {
        listeners.remove(listener)
    }

    private fun createMediaInfo(stream: StreamModel): MediaInfo {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        metadata.putString(MediaMetadata.KEY_TITLE, stream.title)
        metadata.putString(MediaMetadata.KEY_SUBTITLE, stream.metaData?.description)
        
        stream.thumbnailUrl?.let {
            metadata.addImage(WebImage(Uri.parse(it)))
        }

        return MediaInfo.Builder(stream.url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(getContentType(stream.url))
            .setMetadata(metadata)
            .build()
    }

    private fun getContentType(url: String): String {
        return when {
            url.contains(".m3u8") -> "application/x-mpegURL"
            url.contains(".mpd") -> "application/dash+xml"
            url.contains(".mp4") -> "video/mp4"
            else -> "video/mp4"
        }
    }

    fun release() {
        castContext?.sessionManager?.removeSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
    }
}

interface CastListener {
    fun onCastConnected()
    fun onCastDisconnected()
    fun onCastError(message: String)
}
