package com.xtremeiptv.feature.player

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.xtremeiptv.core.designsystem.theme.XtremeIPTVTheme
import com.xtremeiptv.feature.player.manager.PlayerManager
import com.xtremeiptv.feature.player.ui.GestureOverlay
import com.xtremeiptv.feature.player.ui.PlayerControls
import com.xtremeiptv.feature.player.ui.QualitySelector
import com.xtremeiptv.feature.player.ui.SubtitleSelector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    @Inject
    lateinit var playerManager: PlayerManager

    private val viewModel: PlayerViewModel by viewModels()
    private var controlsVisible = true
    private var controlsHideJob = lifecycleScope.launch { }

    companion object {
        private const val EXTRA_STREAM_ID = "stream_id"
        private const val EXTRA_STREAM_URL = "stream_url"
        private const val EXTRA_START_POSITION = "start_position"

        fun createIntent(
            context: Context,
            streamId: String,
            streamUrl: String,
            startPosition: Long = 0
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_STREAM_ID, streamId)
                putExtra(EXTRA_STREAM_URL, streamUrl)
                putExtra(EXTRA_START_POSITION, startPosition)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set fullscreen flags
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val streamId = intent.getStringExtra(EXTRA_STREAM_ID) ?: ""
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""
        val startPosition = intent.getLongExtra(EXTRA_START_POSITION, 0)

        viewModel.loadStream(streamId, streamUrl)

        setContent {
            XtremeIPTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerScreen(
                        viewModel = viewModel,
                        playerManager = playerManager,
                        streamUrl = streamUrl,
                        startPosition = startPosition,
                        onBackPressed = { finish() },
                        onPictureInPicture = { enterPictureInPictureMode() }
                    )
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setPictureInPictureMode(isInPictureInPictureMode)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && viewModel.isPictureInPictureSupported()) {
            val rational = Rational(16, 9)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(rational)
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                viewModel.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                viewModel.seekBackward()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                viewModel.seekForward()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    playerManager: PlayerManager,
    streamUrl: String,
    startPosition: Long,
    onBackPressed: () -> Unit,
    onPictureInPicture: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val playbackState by viewModel.playbackState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isControlsVisible by viewModel.isControlsVisible.collectAsState()
    val showQualitySelector by viewModel.showQualitySelector.collectAsState()
    val showSubtitleSelector by viewModel.showSubtitleSelector.collectAsState()
    val availableTracks by viewModel.availableTracks.collectAsState()

    LaunchedEffect(Unit) {
        playerManager.initialize()
        playerManager.prepareStream(streamUrl, startPositionMs = startPosition)
    }

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Player View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = PlayerView.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Overlay
        GestureOverlay(
            onVolumeChange = { delta -> viewModel.adjustVolume(delta) },
            onBrightnessChange = { delta -> viewModel.adjustBrightness(delta) },
            onSeek = { delta -> viewModel.seekBy(delta) },
            onSingleTap = { viewModel.toggleControls() },
            onDoubleTap = { viewModel.togglePlayPause() },
            isVisible = isControlsVisible && !showQualitySelector && !showSubtitleSelector,
            modifier = Modifier.fillMaxSize()
        )

        // Player Controls
        if (isControlsVisible && !viewModel.isPictureInPictureMode) {
            PlayerControls(
                playbackState = playbackState,
                currentPosition = currentPosition,
                duration = duration,
                isPlaying = isPlaying,
                title = viewModel.streamTitle,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { position -> viewModel.seekTo(position) },
                onSeekForward = { viewModel.seekForward() },
                onSeekBackward = { viewModel.seekBackward() },
                onQualityClick = { viewModel.showQualitySelector() },
                onSubtitleClick = { viewModel.showSubtitleSelector() },
                onAudioTrackClick = { /* TODO */ },
                onCastClick = { /* TODO */ },
                onDownloadClick = { /* TODO */ },
                onRecordClick = { /* TODO */ },
                onPictureInPicture = onPictureInPicture,
                onBackPressed = onBackPressed,
                isLandscape = isLandscape,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Quality Selector
        if (showQualitySelector) {
            QualitySelector(
                tracks = availableTracks,
                onTrackSelected = { trackId -> viewModel.selectTrack(trackId) },
                onDismiss = { viewModel.hideQualitySelector() }
            )
        }

        // Subtitle Selector
        if (showSubtitleSelector) {
            SubtitleSelector(
                tracks = availableTracks,
                onTrackSelected = { trackId -> viewModel.selectSubtitle(trackId) },
                onDismiss = { viewModel.hideSubtitleSelector() }
            )
        }

        // Loading Indicator
        if (playbackState == Player.STATE_BUFFERING) {
            // Show loading indicator
        }

        // Error Message
        viewModel.errorMessage?.let { error ->
            // Show error message
        }
    }
}
