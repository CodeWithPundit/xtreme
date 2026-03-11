package com.xtremeiptv.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xtremeiptv.core.designsystem.theme.*
import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.model.StreamType

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: (StreamType, String) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTv = configuration.screenWidthDp >= 600
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepAbyss)
    ) {
        if (isTv) {
            TvHomeContent(
                uiState = uiState,
                onStreamClick = onStreamClick,
                onViewAllClick = onViewAllClick,
                onProfileClick = onProfileClick
            )
        } else {
            MobileHomeContent(
                uiState = uiState,
                onStreamClick = onStreamClick,
                onViewAllClick = onViewAllClick,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun MobileHomeContent(
    uiState: HomeUiState,
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: (StreamType, String) -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            HeroBanner(
                streams = uiState.featuredStreams,
                onStreamClick = onStreamClick
            )
        }
        
        item {
            ProfileIndicator(
                profileName = uiState.activeProfile?.name ?: "Guest",
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        if (uiState.continueWatching.isNotEmpty()) {
            item {
                ContentRow(
                    title = "Continue Watching",
                    streams = uiState.continueWatching,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.MOVIE, "continue") }
                )
            }
        }
        
        item {
            ContentRow(
                title = "Recommended for You",
                streams = uiState.recommendations,
                onStreamClick = onStreamClick,
                onViewAllClick = { onViewAllClick(StreamType.MOVIE, "recommended") }
            )
        }
        
        item {
            ContentRow(
                title = "Live TV",
                streams = uiState.liveStreams,
                onStreamClick = onStreamClick,
                onViewAllClick = { onViewAllClick(StreamType.LIVE, "all") }
            )
        }
        
        item {
            ContentRow(
                title = "Recent Movies",
                streams = uiState.recentMovies,
                onStreamClick = onStreamClick,
                onViewAllClick = { onViewAllClick(StreamType.MOVIE, "recent") }
            )
        }
        
        item {
            ContentRow(
                title = "Popular Series",
                streams = uiState.popularSeries,
                onStreamClick = onStreamClick,
                onViewAllClick = { onViewAllClick(StreamType.SERIES, "popular") }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvHomeContent(
    uiState: HomeUiState,
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: (StreamType, String) -> Unit,
    onProfileClick: () -> Unit
) {
    TvSurface(
        modifier = Modifier.fillMaxSize(),
        color = DeepAbyss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                TvHeroBanner(
                    streams = uiState.featuredStreams,
                    onStreamClick = onStreamClick
                )
            }
            
            item {
                TvContentRow(
                    title = "Continue Watching",
                    streams = uiState.continueWatching,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.MOVIE, "continue") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Recommended for You",
                    streams = uiState.recommendations,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.MOVIE, "recommended") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Live TV",
                    streams = uiState.liveStreams,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.LIVE, "all") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Movies",
                    streams = uiState.recentMovies,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.MOVIE, "all") }
                )
            }
            
            item {
                TvContentRow(
                    title = "Series",
                    streams = uiState.popularSeries,
                    onStreamClick = onStreamClick,
                    onViewAllClick = { onViewAllClick(StreamType.SERIES, "all") }
                )
            }
        }
    }
}

@Composable
fun HeroBanner(
    streams: List<StreamModel>,
    onStreamClick: (StreamModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (streams.isEmpty()) return
    
    val configuration = LocalConfiguration.current
    val bannerHeight = (configuration.screenHeightDp * 0.45).dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
    ) {
        val currentStream = streams.firstOrNull() ?: return
        
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentStream.thumbnailUrl ?: currentStream.logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
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
                .padding(24.dp)
        ) {
            Text(
                text = currentStream.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = BoneWhite
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            currentStream.metaData?.description?.let { description ->
                Text(
                    text = description.take(150) + if (description.length > 150) "..." else "",
                    fontSize = 14.sp,
                    color = BoneWhite.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onStreamClick(currentStream) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGlow,
                    contentColor = DeepAbyss
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ContentRow(
    title: String,
    streams: List<StreamModel>,
    onStreamClick: (StreamModel) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BoneWhite
            )
            
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "View All",
                    color = EmeraldGlow,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(streams) { stream ->
                StreamCard(
                    stream = stream,
                    onClick = { onStreamClick(stream) },
                    modifier = Modifier.width(140.dp)
                )
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
                .padding(horizontal = 32.dp),
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
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(streams) { stream ->
                TvStreamCard(
                    stream = stream,
                    onClick = { onStreamClick(stream) },
                    modifier = Modifier.width(240.dp)
                )
            }
        }
    }
}

@Composable
fun StreamCard(
    stream: StreamModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stream.thumbnailUrl ?: stream.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (stream.type == StreamType.LIVE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(
                                color = Color.Red,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Text(
                text = stream.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BoneWhite,
                maxLines = 2,
                modifier = Modifier.padding(8.dp)
            )
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
        shape = RoundedCornerShape(12.dp),
        colors = TvCardDefaults.colors(
            containerColor = SunkenTimber
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stream.thumbnailUrl ?: stream.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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

@Composable
fun ProfileIndicator(
    profileName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onProfileClick) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CursedTeal,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profileName.first().toString().uppercase(),
                        color = BoneWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
