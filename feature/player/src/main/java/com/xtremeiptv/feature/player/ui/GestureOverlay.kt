package com.xtremeiptv.feature.player.ui

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GestureOverlay(
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onSeek: (Long) -> Unit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp * configuration.densityDpi / 160
    val screenHeight = configuration.screenHeightDp * configuration.densityDpi / 160
    
    var initialX by remember { mutableStateOf(0f) }
    var initialY by remember { mutableStateOf(0f) }
    var lastDeltaX by remember { mutableStateOf(0f) }
    var lastDeltaY by remember { mutableStateOf(0f) }
    var gestureType by remember { mutableStateOf<GestureType?>(null) }
    var seekJob by remember { mutableStateOf<Job?>(null) }
    
    val gestureDetector = remember {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap()
                return true
            }
            
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onSingleTap()
                return true
            }
        }
    }
    
    val detector = remember {
        GestureDetector(context, gestureDetector)
    }
    
    if (!isVisible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        
                        when (event.type) {
                            PointerEventType.Press -> {
                                initialX = event.changes.first().position.x
                                initialY = event.changes.first().position.y
                                lastDeltaX = 0f
                                lastDeltaY = 0f
                            }
                            
                            PointerEventType.Move -> {
                                val currentX = event.changes.first().position.x
                                val currentY = event.changes.first().position.y
                                
                                val deltaX = currentX - initialX
                                val deltaY = currentY - initialY
                                
                                val deltaXDiff = deltaX - lastDeltaX
                                val deltaYDiff = deltaY - lastDeltaY
                                
                                if (gestureType == null && (abs(deltaX) > 20 || abs(deltaY) > 20)) {
                                    gestureType = when {
                                        abs(deltaX) > abs(deltaY) -> GestureType.SEEK
                                        currentX < screenWidth / 2 -> GestureType.BRIGHTNESS
                                        else -> GestureType.VOLUME
                                    }
                                }
                                
                                when (gestureType) {
                                    GestureType.SEEK -> {
                                        val seekDelta = (deltaXDiff / screenWidth) * (5 * 60 * 1000) // 5 minutes max
                                        onSeek(seekDelta.toLong())
                                    }
                                    GestureType.VOLUME -> {
                                        val volumeDelta = -deltaYDiff / screenHeight
                                        onVolumeChange(volumeDelta)
                                    }
                                    GestureType.BRIGHTNESS -> {
                                        val brightnessDelta = -deltaYDiff / screenHeight
                                        onBrightnessChange(brightnessDelta)
                                    }
                                    null -> {}
                                }
                                
                                lastDeltaX = deltaX
                                lastDeltaY = deltaY
                            }
                            
                            PointerEventType.Release -> {
                                gestureType = null
                            }
                        }
                    }
                }
            }
            .pointerInteropFilter { motionEvent ->
                detector.onTouchEvent(motionEvent)
            }
    )
}

enum class GestureType {
    SEEK,
    VOLUME,
    BRIGHTNESS
}
