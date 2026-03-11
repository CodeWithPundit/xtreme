package com.xtremeiptv.feature.player.manager

import android.content.Context
import android.net.Uri
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacheDataSourceFactory: CacheDataSource.Factory,
    private val bandwidthMeter: DefaultBandwidthMeter
) {
    
    private var exoPlayer: ExoPlayer? = null
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    private val _currentPosition = MutableStateFlow(0L)
    private val _bufferedPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _playWhenReady = MutableStateFlow(false)
    private val _volume = MutableStateFlow(1f)
    private val _playbackSpeed = MutableStateFlow(1f)
    private val _videoSize = MutableStateFlow(VideoSize.UNKNOWN)
    private val _error = MutableStateFlow<PlaybackError?>(null)
    private val _availableTracks = MutableStateFlow<Map<Int, List<TrackInfo>>>(emptyMap())
    private val _selectedTracks = MutableStateFlow<Map<Int, TrackInfo>>(emptyMap())
    
    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _playbackState.value = when (playbackState) {
                Player.STATE_IDLE -> PlaybackState.IDLE
                Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                Player.STATE_READY -> PlaybackState.READY
                Player.STATE_ENDED -> PlaybackState.ENDED
                else -> PlaybackState.IDLE
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _error.value = PlaybackError(
                type = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> ErrorType.IO
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> ErrorType.UNSUPPORTED_FORMAT
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> ErrorType.DECODING
                    PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> ErrorType.BEHIND_LIVE
                    else -> ErrorType.UNKNOWN
                },
                message = error.message,
                errorCode = error.errorCode
            )
        }
        
        override fun onTracksChanged(tracks: Tracks) {
            val trackGroups = tracks.groups
            val available = mutableMapOf<Int, List<TrackInfo>>()
            val selected = mutableMapOf<Int, TrackInfo>()
            
            trackGroups.forEachIndexed { groupIndex, trackGroup ->
                if (!trackGroup.isSelected) return@forEachIndexed
                
                val trackType = when (trackGroup.type) {
                    C.TRACK_TYPE_VIDEO -> TrackType.VIDEO
                    C.TRACK_TYPE_AUDIO -> TrackType.AUDIO
                    C.TRACK_TYPE_TEXT -> TrackType.SUBTITLE
                    else -> null
                }
                
                trackType?.let { type ->
                    val trackInfos = (0 until trackGroup.length).mapNotNull { index ->
                        val format = trackGroup.getTrackFormat(index)
                        TrackInfo(
                            id = "$groupIndex:$index",
                            language = format.language,
                            label = format.label,
                            bitrate = format.bitrate,
                            width = format.width,
                            height = format.height,
                            codec = format.codecs,
                            isSelected = trackGroup.isTrackSelected(index)
                        ).takeIf { it.isValid() }
                    }
                    
                    available[type.ordinal] = trackInfos
                    
                    trackInfos.firstOrNull { it.isSelected }?.let {
                        selected[type.ordinal] = it
                    }
                }
            }
            
            _availableTracks.value = available
            _selectedTracks.value = selected
        }
        
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            _videoSize.value = videoSize
        }
    }
    
    private val positionUpdateJob = Job()
    private val positionUpdateScope = CoroutineScope(Dispatchers.Main + positionUpdateJob)
    
    fun initialize() {
        if (exoPlayer == null) {
            val trackSelector = DefaultTrackSelector(context).apply {
                setParameters(
                    buildUponParameters {
                        setAllowVideoMixedMimeTypeAdaptiveness(true)
                        setAllowAudioMixedMimeTypeAdaptiveness(true)
                        setForceHighestSupportedBitrate(false)
                    }
                )
            }
            
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
            
            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setBandwidthMeter(bandwidthMeter)
                .setSeekForwardIncrementMs(10000)
                .setSeekBackIncrementMs(10000)
                .build()
                .apply {
                    addListener(playbackStateListener)
                    setHandleAudioBecomingNoisy(true)
                    setWakeMode(C.WAKE_MODE_NETWORK)
                }
            
            startPositionUpdates()
        }
    }
    
    fun setPlayerView(playerView: PlayerView) {
        playerView.player = exoPlayer
        playerView.useController = true
        playerView.setShowSubtitleButton(true)
        playerView.setShowFastForwardButton(true)
        playerView.setShowRewindButton(true)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
    }
    
    fun prepareStream(
        url: String,
        headers: Map<String, String> = emptyMap(),
        startPositionMs: Long = 0
    ) {
        val player = exoPlayer ?: return
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("XtremeIPTV")
            .setDefaultRequestProperties(headers)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
        
        val mediaSource = createMediaSource(url, dataSourceFactory)
        
        player.setMediaSource(mediaSource)
        player.seekTo(startPositionMs)
        player.prepare()
    }
    
    private fun createMediaSource(
        url: String,
        dataSourceFactory: DataSource.Factory
    ): MediaSource {
        val uri = Uri.parse(url)
        
        return when {
            url.contains(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
            url.contains(".mpd") -> {
                DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
        }
    }
    
    fun play() {
        exoPlayer?.play()
        _playWhenReady.value = true
    }
    
    fun pause() {
        exoPlayer?.pause()
        _playWhenReady.value = false
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }
    
    fun seekForward() {
        exoPlayer?.seekForward()
    }
    
    fun seekBackward() {
        exoPlayer?.seekBack()
    }
    
    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
        _volume.value = volume
    }
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed.coerceIn(0.25f, 2f))
        _playbackSpeed.value = speed
    }
    
    fun selectTrack(trackType: TrackType, trackId: String) {
        val player = exoPlayer ?: return
        val tracks = player.currentTracks
        
        tracks.groups.forEachIndexed { groupIndex, trackGroup ->
            if (trackGroup.type == when (trackType) {
                TrackType.VIDEO -> C.TRACK_TYPE_VIDEO
                TrackType.AUDIO -> C.TRACK_TYPE_AUDIO
                TrackType.SUBTITLE -> C.TRACK_TYPE_TEXT
            }) {
                for (i in 0 until trackGroup.length) {
                    if ("$groupIndex:$i" == trackId) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setSelectionOverride(
                                groupIndex,
                                trackGroup,
                                TrackSelectionOverride(trackGroup, listOf(i))
                            )
                            .build()
                        return
                    }
                }
            }
        }
    }
    
    fun release() {
        positionUpdateJob.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
    
    private fun startPositionUpdates() {
        positionUpdateScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                    _bufferedPosition.value = player.bufferedPosition
                    _duration.value = player.duration
                }
                delay(500)
            }
        }
    }
    
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    val bufferedPosition: StateFlow<Long> = _bufferedPosition.asStateFlow()
    val duration: StateFlow<Long> = _duration.asStateFlow()
    val playWhenReady: StateFlow<Boolean> = _playWhenReady.asStateFlow()
    val volume: StateFlow<Float> = _volume.asStateFlow()
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    val videoSize: StateFlow<VideoSize> = _videoSize.asStateFlow()
    val error: StateFlow<PlaybackError?> = _error.asStateFlow()
    val availableTracks: StateFlow<Map<Int, List<TrackInfo>>> = _availableTracks.asStateFlow()
    val selectedTracks: StateFlow<Map<Int, TrackInfo>> = _selectedTracks.asStateFlow()
}

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

enum class TrackType {
    VIDEO,
    AUDIO,
    SUBTITLE
}

enum class ErrorType {
    IO,
    DECODING,
    UNSUPPORTED_FORMAT,
    BEHIND_LIVE,
    UNKNOWN
}

data class PlaybackError(
    val type: ErrorType,
    val message: String?,
    val errorCode: Int
)

data class TrackInfo(
    val id: String,
    val language: String?,
    val label: String?,
    val bitrate: Int?,
    val width: Int?,
    val height: Int?,
    val codec: String?,
    val isSelected: Boolean
) {
    fun isValid(): Boolean {
        return when {
            bitrate != null && bitrate > 0 -> true
            width != null && height != null && width > 0 && height > 0 -> true
            language != null && language.isNotEmpty() -> true
            else -> false
        }
    }
}
