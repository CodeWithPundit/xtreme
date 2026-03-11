package com.xtremeiptv.feature.epg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xtremeiptv.core.designsystem.theme.*
import com.xtremeiptv.core.domain.model.EpgChannel
import com.xtremeiptv.core.domain.model.EpgProgram
import java.util.*

@Composable
fun EPGGridView(
    channels: List<EpgChannel>,
    programs: Map<String, List<EpgProgram>>,
    currentTime: Date,
    onProgramClick: (EpgProgram) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeSlots = remember(currentTime) {
        generateTimeSlots(currentTime)
    }
    
    val gridState = rememberEpgGridState()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Time Header
        TimeHeaderRow(
            timeSlots = timeSlots,
            scrollState = gridState.horizontalScrollState,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
        
        // Channel List with Programs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Channel Names Column
            ChannelNamesColumn(
                channels = channels,
                scrollState = gridState.verticalScrollState,
                modifier = Modifier.width(120.dp)
            )
            
            // Programs Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                LazyRow(
                    state = gridState.horizontalScrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        LazyColumn(
                            state = gridState.verticalScrollState,
                            modifier = Modifier.width((timeSlots.size * 100).dp)
                        ) {
                            items(channels) { channel ->
                                ProgramRow(
                                    channel = channel,
                                    programs = programs[channel.id] ?: emptyList(),
                                    timeSlots = timeSlots,
                                    currentTime = currentTime,
                                    onProgramClick = onProgramClick,
                                    textMeasurer = textMeasurer,
                                    density = density,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                )
                            }
                        }
                    }
                }
                
                // Current Time Indicator
                CurrentTimeIndicator(
                    currentTime = currentTime,
                    timeSlots = timeSlots,
                    horizontalScrollState = gridState.horizontalScrollState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TimeHeaderRow(
    timeSlots: List<Date>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = scrollState,
        modifier = modifier
    ) {
        item {
            Spacer(modifier = Modifier.width(120.dp))
        }
        
        items(timeSlots) { time ->
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .drawBehind {
                        drawLine(
                            color = BoneWhite.copy(alpha = 0.3f),
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTimeSlot(time),
                    color = BoneWhite,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ChannelNamesColumn(
    channels: List<EpgChannel>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier
    ) {
        items(channels) { channel ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .drawBehind {
                        drawLine(
                            color = BoneWhite.copy(alpha = 0.3f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.name,
                    color = BoneWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun ProgramRow(
    channel: EpgChannel,
    programs: List<EpgProgram>,
    timeSlots: List<Date>,
    currentTime: Date,
    onProgramClick: (EpgProgram) -> Unit,
    textMeasurer: TextMeasurer,
    density: Density,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        programs.forEach { program ->
            ProgramCell(
                program = program,
                timeSlots = timeSlots,
                currentTime = currentTime,
                onClick = { onProgramClick(program) },
                textMeasurer = textMeasurer,
                density = density
            )
        }
    }
}

@Composable
fun ProgramCell(
    program: EpgProgram,
    timeSlots: List<Date>,
    currentTime: Date,
    onClick: () -> Unit,
    textMeasurer: TextMeasurer,
    density: Density
) {
    val startSlot = findTimeSlotIndex(program.startTime, timeSlots)
    val endSlot = findTimeSlotIndex(program.endTime, timeSlots)
    val width = (endSlot - startSlot) * 100
    val isNow = program.isCurrentlyPlaying
    val isPast = program.endTime.before(currentTime)
    
    Box(
        modifier = Modifier
            .width(with(density) { width.dp })
            .fillMaxHeight()
            .clickable { onClick() }
            .background(
                color = when {
                    isNow -> EmeraldGlow.copy(alpha = 0.3f)
                    isPast -> SunkenTimber.copy(alpha = 0.5f)
                    else -> SunkenTimber
                }
            )
            .drawBehind {
                drawRect(
                    color = BoneWhite.copy(alpha = 0.1f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = program.title,
            color = if (isNow) EmeraldGlow else BoneWhite,
            fontSize = 12.sp,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun CurrentTimeIndicator(
    currentTime: Date,
    timeSlots: List<Date>,
    horizontalScrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val slotIndex = findTimeSlotIndex(currentTime, timeSlots)
    val slotPosition = slotIndex * 100
    val currentSlot = timeSlots.getOrNull(slotIndex) ?: return
    
    val progress = (currentTime.time - currentSlot.time) / 
                   (60 * 60 * 1000) // 1 hour in milliseconds
    
    Canvas(
        modifier = modifier
    ) {
        val x = slotPosition + (progress * 100).toFloat()
        
        drawLine(
            color = Error,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 2.dp.toPx()
        )
        
        drawCircle(
            color = Error,
            radius = 4.dp.toPx(),
            center = Offset(x, 0f)
        )
    }
}

@Stable
class EpgGridState(
    val horizontalScrollState: LazyListState,
    val verticalScrollState: LazyListState
)

@Composable
fun rememberEpgGridState(): EpgGridState {
    val horizontalScrollState = rememberLazyListState()
    val verticalScrollState = rememberLazyListState()
    
    return remember(horizontalScrollState, verticalScrollState) {
        EpgGridState(horizontalScrollState, verticalScrollState)
    }
}

fun generateTimeSlots(currentTime: Date): List<Date> {
    val calendar = Calendar.getInstance().apply {
        time = currentTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    return List(24 * 7) { i ->
        calendar.timeInMillis + i * 60 * 60 * 1000
    }.map { Date(it) }
}

fun findTimeSlotIndex(time: Date, timeSlots: List<Date>): Int {
    return timeSlots.indexOfFirst { slot ->
        slot.time <= time.time && time.time < slot.time + 60 * 60 * 1000
    }.takeIf { it >= 0 } ?: 0
}

fun formatTimeSlot(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    return String.format("%02d:00", hour)
}
