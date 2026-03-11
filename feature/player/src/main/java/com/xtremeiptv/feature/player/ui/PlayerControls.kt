package com.xtremeiptv.feature.player.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.xtremeiptv.core.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControls(
    playbackState: Int,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    title: String,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onQualityClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioTrackClick: () -> Unit,
    onCastClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onRecordClick: () -> Unit,
    onPictureInPicture: () -> Unit,
    onBackPressed: () -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { /* Handled by parent */ }
                )
            }
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = BoneWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                
                Text(
                    text = title,
                    color = BoneWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(
                        onClick = onCastClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = BoneWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Cast"
                        )
                    }
                    
                    IconButton(
                        onClick = onPictureInPicture,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = BoneWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Picture in Picture"
                        )
                    }
                }
            }
        }

        // Center Controls
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind
                IconButton(
                    onClick = onSeekBackward,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = BoneWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(EmeraldGlow),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = DeepAbyss
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Forward
                IconButton(
                    onClick = onSeekForward,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = BoneWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Progress Bar
            PlayerProgressBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = onSeekTo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            )
        }

        // Bottom Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.Settings,
                    label = "Quality",
                    onClick = onQualityClick
                )
                
                ActionButton(
                    icon = Icons.Default.Subtitles,
                    label = "Subtitles",
                    onClick = onSubtitleClick
                )
                
                ActionButton(
                    icon = Icons.Default.Audiotrack,
                    label = "Audio",
                    onClick = onAudioTrackClick
                )
                
                ActionButton(
                    icon = Icons.Default.Download,
                    label = "Download",
                    onClick = onDownloadClick
                )
                
                ActionButton(
                    icon = Icons.Default.FiberManualRecord,
                    label = "Record",
                    onClick = onRecordClick
                )
            }
        }

        // Loading Indicator
        if (playbackState == Player.STATE_BUFFERING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = EmeraldGlow,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = BoneWhite
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            color = BoneWhite,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PlayerProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val positionMinutes = (currentPosition / 1000 / 60).toInt()
    val positionSeconds = (currentPosition / 1000 % 60).toInt()
    val durationMinutes = (duration / 1000 / 60).toInt()
    val durationSeconds = (duration / 1000 % 60).toInt()
    
    Column(
        modifier = modifier
    ) {
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EmeraldGlow,
                activeTrackColor = EmeraldGlow,
                inactiveTrackColor = BoneWhite.copy(alpha = 0.3f)
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format("%02d:%02d", positionMinutes, positionSeconds),
                color = BoneWhite,
                fontSize = 12.sp
            )
            
            Text(
                text = String.format("%02d:%02d", durationMinutes, durationSeconds),
                color = BoneWhite,
                fontSize = 12.sp
            )
        }
    }
}
