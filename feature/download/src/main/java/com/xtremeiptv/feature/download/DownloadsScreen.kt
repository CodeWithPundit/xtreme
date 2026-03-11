package com.xtremeiptv.feature.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.xtremeiptv.core.designsystem.theme.*
import com.xtremeiptv.core.domain.model.DownloadItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onDownloadClick: (DownloadItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::clearCompleted,
                        enabled = uiState.hasCompletedDownloads
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Completed")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepAbyss,
                    titleContentColor = BoneWhite,
                    navigationIconContentColor = BoneWhite,
                    actionIconContentColor = BoneWhite
                )
            )
        },
        containerColor = DeepAbyss
    ) { paddingValues ->
        if (uiState.downloads.isEmpty()) {
            EmptyDownloadsScreen(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Downloads
                if (uiState.activeDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Downloads",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoneWhite,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(uiState.activeDownloads) { download ->
                        ActiveDownloadItem(
                            download = download,
                            onPause = viewModel::pauseDownload,
                            onResume = viewModel::resumeDownload,
                            onCancel = viewModel::cancelDownload
                        )
                    }
                }
                
                // Completed Downloads
                if (uiState.completedDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloaded",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoneWhite,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(uiState.completedDownloads) { download ->
                        CompletedDownloadItem(
                            download = download,
                            onClick = { onDownloadClick(download) },
                            onDelete = viewModel::deleteDownload
                        )
                    }
                }
                
                // Storage Info
                item {
                    StorageInfoCard(
                        usedSpace = uiState.usedSpace,
                        totalSpace = uiState.totalSpace
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDownloadsScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                tint = BoneWhite.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Downloads",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BoneWhite
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Movies and series you download will appear here",
                fontSize = 14.sp,
                color = BoneWhite.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ActiveDownloadItem(
    download: DownloadItem,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                AsyncImage(
                    model = download.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = download.title,
                        color = BoneWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    
                    Text(
                        text = download.quality,
                        color = BoneWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                // Action Buttons
                Row {
                    IconButton(
                        onClick = {
                            if (download.isPaused) {
                                onResume(download.id)
                            } else {
                                onPause(download.id)
                            }
                        }
                    ) {
                        Icon(
                            if (download.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (download.isPaused) "Resume" else "Pause",
                            tint = EmeraldGlow
                        )
                    }
                    
                    IconButton(
                        onClick = { onCancel(download.id) }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = download.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = EmeraldGlow,
                trackColor = BoneWhite.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${download.progress}%",
                    color = EmeraldGlow,
                    fontSize = 12.sp
                )
                
                Text(
                    text = download.speed,
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                Text(
                    text = "${download.downloadedSize} / ${download.totalSize}",
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CompletedDownloadItem(
    download: DownloadItem,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = download.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = download.title,
                    color = BoneWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Text(
                    text = "${download.quality} • ${download.totalSize}",
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                Text(
                    text = "Downloaded ${formatDate(download.completionTime)}",
                    color = BoneWhite.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
            
            // Play Icon
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = EmeraldGlow,
                modifier = Modifier.size(32.dp)
            )
            
            // Delete Button
            IconButton(
                onClick = { onDelete(download.id) }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = BoneWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StorageInfoCard(
    usedSpace: String,
    totalSpace: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CursedTeal.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = EmeraldGlow
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Storage",
                    color = BoneWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "$usedSpace / $totalSpace",
                color = BoneWhite,
                fontSize = 14.sp
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> "Today"
        diff < 2 * 24 * 60 * 60 * 1000 -> "Yesterday"
        else -> SimpleDateFormat("MMM dd", Locale.US).format(date)
    }
}
