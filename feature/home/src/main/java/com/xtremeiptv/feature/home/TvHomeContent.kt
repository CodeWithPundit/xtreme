package com.xtremeiptv.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.xtremeiptv.core.designsystem.theme.BoneWhite
import com.xtremeiptv.core.designsystem.theme.DeepAbyss
import com.xtremeiptv.core.designsystem.theme.EmeraldGlow
import com.xtremeiptv.core.domain.model.StreamModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeContent(
    uiState: HomeUiState,
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TvSurface(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester),
        color = DeepAbyss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                TvHeroBanner(
                    streams = uiState.featuredStreams,
                    onStreamClick = onStreamClick
                )
            }
            
            item {
                TvProfileRow(
                    profileName = uiState.activeProfile?.name ?: "Guest",
                    onProfileClick = onProfileClick,
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp)
                )
            }
            
            if (uiState.continueWatching.isNotEmpty()) {
                item {
                    TvContentRow(
                        title = "Continue Watching",
                        streams = uiState.continueWatching,
                        onStreamClick = onStreamClick,
                        onViewAllClick = { onViewAllClick("continue") }
                    )
                }
            }
            
            item {
                TvContentRow(
                    title = "Recommended for You",
                    streams = uiState.recommendations,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick("recommended") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Live TV",
                    streams = uiState.liveStreams,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick("live") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Popular Movies",
                    streams = uiState.recentMovies,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick("movies") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Trending Series",
                    streams = uiState.popularSeries,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick("series") }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHeroBanner(
    streams: List<StreamModel>,
    onStreamClick: (StreamModel) -> Unit
) {
    if (streams.isEmpty()) return
    
    val currentStream = streams.firstOrNull() ?: return
    
    TvSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        color = DeepAbyss
    ) {
        Box {
            AsyncImage(
                model = currentStream.thumbnailUrl ?: currentStream.logoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DeepAbyss.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(48.dp)
            ) {
                TvText(
                    text = currentStream.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = BoneWhite
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                currentStream.metaData?.description?.let { description ->
                    TvText(
                        text = description.take(200) + if (description.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BoneWhite.copy(alpha = 0.8f),
                        maxLines = 3
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TvButton(
                    onClick = { onStreamClick(currentStream) },
                    colors = TvButtonDefaults.filledTvButtonColors(
                        containerColor = EmeraldGlow,
                        contentColor = DeepAbyss
                    )
                ) {
                    TvText("Play Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvContentRow(
    title: String,
    streams: List<StreamModel>,
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvText(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = BoneWhite
            )
            
            TvButton(
                onClick = onViewAllClick,
                colors = TvButtonDefaults.filledTvButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = EmeraldGlow
                )
            ) {
                TvText("View All", style = MaterialTheme.typography.bodyLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(streams) { stream ->
                TvStreamCard(
                    stream = stream,
                    onClick = { onStreamClick(stream) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvStreamCard(
    stream: StreamModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvCard(
        onClick = onClick,
        modifier = modifier,
        scale = TvCardScale.Normal,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = stream.thumbnailUrl ?: stream.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (stream.type == StreamType.LIVE) {
                    TvSurface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                color = Color.Red,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        TvText(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            TvText(
                text = stream.title,
                style = MaterialTheme.typography.bodyLarge,
                color = BoneWhite,
                maxLines = 2,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvProfileRow(
    profileName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvButton(
            onClick = onProfileClick,
            colors = TvButtonDefaults.filledTvButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            TvSurface(
                shape = CircleShape,
                color = CursedTeal,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TvText(
                        text = profileName.first().toString().uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = BoneWhite
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            TvText(
                text = profileName,
                style = MaterialTheme.typography.bodyLarge,
                color = BoneWhite
            )
        }
    }
}
