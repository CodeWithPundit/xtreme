package com.xtremeiptv.feature.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.xtremeiptv.core.designsystem.theme.*
import com.xtremeiptv.core.domain.model.Series
import com.xtremeiptv.core.domain.model.Season
import com.xtremeiptv.core.domain.model.Episode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: String,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEpisodeClick: (Episode) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val bannerHeight = configuration.screenHeightDp * 0.4
    
    LaunchedEffect(seriesId) {
        viewModel.loadSeries(seriesId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        uiState.series?.backdropPath?.let { backdropUrl ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight.dp)
            ) {
                AsyncImage(
                    model = backdropUrl,
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
                                    DeepAbyss
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = (bannerHeight - 60).dp, bottom = 16.dp)
        ) {
            if (uiState.series != null) {
                item {
                    SeriesInfo(
                        series = uiState.series,
                        isFavorite = uiState.isFavorite,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onPlayRandom = viewModel::playRandomEpisode
                    )
                }

                // Seasons
                uiState.seasons.forEach { season ->
                    item {
                        SeasonSection(
                            season = season,
                            currentSeason = uiState.currentSeason,
                            isExpanded = season.seasonNumber == uiState.currentSeason,
                            onSeasonClick = viewModel::selectSeason,
                            onEpisodeClick = onEpisodeClick
                        )
                    }
                }

                // Similar Series
                if (uiState.similarSeries.isNotEmpty()) {
                    item {
                        SimilarSeriesSection(
                            series = uiState.similarSeries,
                            onSeriesClick = { /* Navigate to series detail */ }
                        )
                    }
                }
            }
        }

        // Top Bar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = BoneWhite
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Share */ }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = BoneWhite
                    }
                }
                IconButton(onClick = { /* Download */ }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        tint = BoneWhite
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SeriesInfo(
    series: Series,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayRandom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = series.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BoneWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Metadata Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            series.year?.let {
                InfoChip(text = it.toString())
            }
            
            series.rating?.let {
                InfoChip(
                    text = "$it ★",
                    icon = Icons.Default.Star
                )
            }
            
            series.genres?.take(2)?.forEach { genre ->
                InfoChip(text = genre)
            }
            
            InfoChip(text = "${series.totalSeasons} Seasons")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        series.description?.let {
            Text(
                text = it,
                color = BoneWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayRandom,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGlow,
                    contentColor = DeepAbyss
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Random Episode")
            }
            
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = SunkenTimber,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) HoardersGold else BoneWhite
                )
            }
        }
    }
}

@Composable
fun SeasonSection(
    season: Season,
    currentSeason: Int,
    isExpanded: Boolean,
    onSeasonClick: (Int) -> Unit,
    onEpisodeClick: (Episode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Season Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeasonClick(season.seasonNumber) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Season ${season.seasonNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BoneWhite
                    )
                    Text(
                        text = "${season.episodes.size} Episodes",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = BoneWhite
                )
            }
            
            // Episodes List
            if (isExpanded) {
                season.episodes.forEach { episode ->
                    EpisodeItem(
                        episode = episode,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeItem(
    episode: Episode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Episode Number
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = CursedTeal,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.episodeNumber.toString(),
                color = BoneWhite,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Episode Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = episode.title,
                color = BoneWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            episode.runtime?.let {
                Text(
                    text = "${it} min",
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
        
        // Play Button
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = EmeraldGlow,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SimilarSeriesSection(
    series: List<Series>,
    onSeriesClick: (Series) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "Similar Series",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BoneWhite,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(series) { item ->
                SimilarSeriesCard(
                    series = item,
                    onClick = { onSeriesClick(item) }
                )
            }
        }
    }
}

@Composable
fun SimilarSeriesCard(
    series: Series,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column {
            AsyncImage(
                model = series.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            
            Text(
                text = series.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BoneWhite,
                maxLines = 2,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun InfoChip(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CursedTeal.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HoardersGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = BoneWhite,
                fontSize = 12.sp
            )
        }
    }
}
