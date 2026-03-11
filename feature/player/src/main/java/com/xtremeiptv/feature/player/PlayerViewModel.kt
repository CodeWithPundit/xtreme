package com.xtremeiptv.feature.player

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.xtremeiptv.core.common.analytics.AnalyticsManager
import com.xtremeiptv.core.data.repository.StreamRepository
import com.xtremeiptv.feature.player.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val playerManager: PlayerManager,
    private val streamRepository: StreamRepository,
    private val analyticsManager: AnalyticsManager
) : AndroidViewModel(application) {

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isControlsVisible = MutableStateFlow(true)
    val isControlsVisible: StateFlow<Boolean> = _isControlsVisible

    private val _showQualitySelector = MutableStateFlow(false)
    val showQualitySelector: StateFlow<Boolean> = _showQualitySelector

    private val _showSubtitleSelector = MutableStateFlow(false)
    val showSubtitleSelector: StateFlow<Boolean> = _showSubtitleSelector

    private val _availableTracks = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val availableTracks: StateFlow<Map<String, List<String>>> = _availableTracks

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _streamTitle = MutableStateFlow("")
    val streamTitle: StateFlow<String> = _streamTitle

    private val _isPictureInPictureMode = MutableStateFlow(false)
    val isPictureInPictureMode: StateFlow<Boolean> = _isPictureInPictureMode

    private var hideControlsJob = viewModelScope.launch {}

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            combine(
                playerManager.playbackState,
                playerManager.currentPosition,
                playerManager.duration,
                playerManager.playWhenReady,
                playerManager.availableTracks
            ) { state, position, duration, playing, tracks ->
                _playbackState.value = state
                _currentPosition.value = position
                _duration.value = duration
                _isPlaying.value = playing && state == Player.STATE_READY
                _availableTracks.value = tracks
            }.collect {}
        }
    }

    fun loadStream(streamId: String, streamUrl: String) {
        viewModelScope.launch {
            _streamTitle.value = "Loading..."
            
            streamRepository.getStreamDetails(streamId).collect { stream ->
                stream?.let {
                    _streamTitle.value = it.title
                    analyticsManager.logStreamPlay(it, "")
                }
            }
            
            playerManager.prepareStream(streamUrl)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            playerManager.pause()
            analyticsManager.logStreamPause(_streamTitle.value, _currentPosition.value)
        } else {
            playerManager.play()
        }
    }

    fun seekTo(position: Long) {
        playerManager.seekTo(position)
    }

    fun seekForward() {
        playerManager.seekForward()
        resetControlsHideTimer()
    }

    fun seekBackward() {
        playerManager.seekBackward()
        resetControlsHideTimer()
    }

    fun seekBy(deltaMs: Long) {
        val newPosition = (_currentPosition.value + deltaMs).coerceIn(0, _duration.value)
        playerManager.seekTo(newPosition)
    }

    fun adjustVolume(delta: Float) {
        val currentVolume = playerManager.volume.value
        val newVolume = (currentVolume + delta).coerceIn(0f, 1f)
        playerManager.setVolume(newVolume)
    }

    fun adjustBrightness(delta: Float) {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        
        try {
            val currentBrightness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            
            val newBrightness = (currentBrightness + (delta * 255)).coerceIn(0, 255)
            
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                newBrightness.toInt()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleControls() {
        _isControlsVisible.value = !_isControlsVisible.value
        if (_isControlsVisible.value) {
            resetControlsHideTimer()
        } else {
            hideControlsJob.cancel()
        }
    }

    private fun resetControlsHideTimer() {
        hideControlsJob.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(3000)
            _isControlsVisible.value = false
        }
    }

    fun showQualitySelector() {
        _showQualitySelector.value = true
        _isControlsVisible.value = true
    }

    fun hideQualitySelector() {
        _showQualitySelector.value = false
        resetControlsHideTimer()
    }

    fun showSubtitleSelector() {
        _showSubtitleSelector.value = true
        _isControlsVisible.value = true
    }

    fun hideSubtitleSelector() {
        _showSubtitleSelector.value = false
        resetControlsHideTimer()
    }

    fun selectTrack(trackId: String) {
        playerManager.selectTrack(trackId)
        hideQualitySelector()
    }

    fun selectSubtitle(trackId: String) {
        playerManager.selectSubtitle(trackId)
        hideSubtitleSelector()
    }

    fun setPictureInPictureMode(enabled: Boolean) {
        _isPictureInPictureMode.value = enabled
        if (enabled) {
            analyticsManager.logPipEntered()
        } else {
            analyticsManager.logPipExited()
        }
    }

    fun isPictureInPictureSupported(): Boolean {
        return getApplication<Application>().packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
