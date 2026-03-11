package com.xtremeiptv.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xtremeiptv.core.designsystem.theme.*
import com.xtremeiptv.core.domain.model.PlayerSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepAbyss,
                    titleContentColor = BoneWhite,
                    navigationIconContentColor = BoneWhite
                )
            )
        },
        containerColor = DeepAbyss
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Settings
            item {
                SettingsCategory(title = "Player Settings")
            }
            
            item {
                SettingsSwitch(
                    title = "Auto Play",
                    subtitle = "Automatically start playback when opening stream",
                    checked = uiState.autoPlay,
                    onCheckedChange = viewModel::setAutoPlay
                )
            }
            
            item {
                SettingsSwitch(
                    title = "Background Play",
                    subtitle = "Continue playing when app is in background",
                    checked = uiState.backgroundPlay,
                    onCheckedChange = viewModel::setBackgroundPlay
                )
            }
            
            item {
                SettingsSlider(
                    title = "Buffer Size",
                    subtitle = "Adjust playback buffer size",
                    value = uiState.bufferSize,
                    valueRange = 1f..10f,
                    onValueChange = viewModel::setBufferSize,
                    valueFormatter = { "${it.toInt()} MB" }
                )
            }
            
            item {
                SettingsDropdown(
                    title = "Default Quality",
                    subtitle = "Preferred video quality",
                    options = listOf("Auto", "240p", "360p", "480p", "720p", "1080p", "4K"),
                    selectedOption = uiState.defaultQuality,
                    onOptionSelected = viewModel::setDefaultQuality
                )
            }
            
            // Download Settings
            item {
                SettingsCategory(title = "Download Settings")
            }
            
            item {
                SettingsSwitch(
                    title = "Download Only on Wi-Fi",
                    subtitle = "Prevent downloads on mobile data",
                    checked = uiState.downloadOnlyWifi,
                    onCheckedChange = viewModel::setDownloadOnlyWifi
                )
            }
            
            item {
                SettingsDropdown(
                    title = "Download Quality",
                    subtitle = "Quality for offline downloads",
                    options = listOf("Same as streaming", "240p", "360p", "480p", "720p", "1080p"),
                    selectedOption = uiState.downloadQuality,
                    onOptionSelected = viewModel::setDownloadQuality
                )
            }
            
            item {
                SettingsInfo(
                    title = "Storage Location",
                    subtitle = uiState.storageLocation,
                    onClick = viewModel::changeStorageLocation
                )
            }
            
            item {
                SettingsInfo(
                    title = "Available Storage",
                    subtitle = uiState.availableStorage
                )
            }
            
            // EPG Settings
            item {
                SettingsCategory(title = "EPG Settings")
            }
            
            item {
                SettingsSwitch(
                    title = "Auto Refresh EPG",
                    subtitle = "Automatically update program guide",
                    checked = uiState.autoRefreshEpg,
                    onCheckedChange = viewModel::setAutoRefreshEpg
                )
            }
            
            item {
                SettingsSlider(
                    title = "EPG Retention Days",
                    subtitle = "Keep EPG data for X days",
                    value = uiState.epgRetentionDays.toFloat(),
                    valueRange = 1f..14f,
                    steps = 13,
                    onValueChange = { viewModel.setEpgRetentionDays(it.toInt()) },
                    valueFormatter = { "${it.toInt()} days" }
                )
            }
            
            // Parental Controls
            item {
                SettingsCategory(title = "Parental Controls")
            }
            
            item {
                SettingsSwitch(
                    title = "Enable Parental Controls",
                    subtitle = "Restrict access to adult content",
                    checked = uiState.parentalControlsEnabled,
                    onCheckedChange = viewModel::setParentalControlsEnabled
                )
            }
            
            if (uiState.parentalControlsEnabled) {
                item {
                    SettingsButton(
                        title = "Change PIN",
                        onClick = viewModel::changePin
                    )
                }
                
                item {
                    SettingsDropdown(
                        title = "Content Restriction Level",
                        subtitle = "Maximum allowed content rating",
                        options = listOf("All", "PG-13", "R", "NC-17"),
                        selectedOption = uiState.contentRestriction,
                        onOptionSelected = viewModel::setContentRestriction
                    )
                }
            }
            
            // Network Settings
            item {
                SettingsCategory(title = "Network Settings")
            }
            
            item {
                SettingsSwitch(
                    title = "Use Mobile Data",
                    subtitle = "Allow streaming on mobile networks",
                    checked = uiState.useMobileData,
                    onCheckedChange = viewModel::setUseMobileData
                )
            }
            
            item {
                SettingsInput(
                    title = "User Agent",
                    subtitle = "Custom user agent for requests",
                    value = uiState.userAgent,
                    onValueChange = viewModel::setUserAgent
                )
            }
            
            item {
                SettingsInput(
                    title = "Proxy Server",
                    subtitle = "Optional proxy for all requests",
                    value = uiState.proxyUrl,
                    onValueChange = viewModel::setProxyUrl
                )
            }
            
            // About Section
            item {
                SettingsCategory(title = "About")
            }
            
            item {
                SettingsInfo(
                    title = "Version",
                    subtitle = uiState.appVersion
                )
            }
            
            item {
                SettingsButton(
                    title = "Terms of Service",
                    onClick = viewModel::openTermsOfService
                )
            }
            
            item {
                SettingsButton(
                    title = "Privacy Policy",
                    onClick = viewModel::openPrivacyPolicy
                )
            }
            
            item {
                SettingsButton(
                    title = "Open Source Licenses",
                    onClick = viewModel::openLicenses
                )
            }
            
            item {
                SettingsButton(
                    title = "Check for Updates",
                    onClick = viewModel::checkForUpdates
                )
            }
            
            // Danger Zone
            item {
                SettingsCategory(title = "Danger Zone", isDanger = true)
            }
            
            item {
                SettingsButton(
                    title = "Clear All Cache",
                    subtitle = "Delete all cached data",
                    isDanger = true,
                    onClick = viewModel::clearAllCache
                )
            }
            
            item {
                SettingsButton(
                    title = "Clear Download History",
                    subtitle = "Remove all download records",
                    isDanger = true,
                    onClick = viewModel::clearDownloadHistory
                )
            }
            
            item {
                SettingsButton(
                    title = "Reset All Settings",
                    subtitle = "Restore default settings",
                    isDanger = true,
                    onClick = viewModel::resetAllSettings
                )
            }
            
            item {
                SettingsButton(
                    title = "Delete All Profiles",
                    subtitle = "Remove all profiles and data",
                    isDanger = true,
                    onClick = viewModel::deleteAllProfiles
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDanger) Error else EmeraldGlow,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = BoneWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = BoneWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EmeraldGlow,
                    checkedTrackColor = EmeraldGlow.copy(alpha = 0.5f),
                    uncheckedThumbColor = BoneWhite,
                    uncheckedTrackColor = BoneWhite.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun SettingsSlider(
    title: String,
    subtitle: String? = null,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String = { "${it.toInt()}" },
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = BoneWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = BoneWhite.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                Text(
                    text = valueFormatter(value),
                    color = EmeraldGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldGlow,
                    activeTrackColor = EmeraldGlow,
                    inactiveTrackColor = BoneWhite.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SettingsDropdown(
    title: String,
    subtitle: String? = null,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = BoneWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = BoneWhite.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Box {
                    TextButton(
                        onClick = { expanded = true }
                    ) {
                        Text(
                            text = selectedOption,
                            color = EmeraldGlow
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = EmeraldGlow
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = SunkenTimber
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = BoneWhite) },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsInput(
    title: String,
    subtitle: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = BoneWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BoneWhite,
                    unfocusedTextColor = BoneWhite,
                    focusedBorderColor = EmeraldGlow,
                    unfocusedBorderColor = BoneWhite.copy(alpha = 0.3f),
                    focusedLabelColor = EmeraldGlow,
                    unfocusedLabelColor = BoneWhite
                )
            )
        }
    }
}

@Composable
fun SettingsInfo(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (onClick != null) {
                    it.clickable { onClick() }
                } else {
                    it
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = SunkenTimber
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = BoneWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = BoneWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = BoneWhite
                )
            }
        }
    }
}

@Composable
fun SettingsButton(
    title: String,
    subtitle: String? = null,
    isDanger: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger) Error.copy(alpha = 0.1f) else SunkenTimber
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = if (isDanger) Error else BoneWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (isDanger) Error.copy(alpha = 0.7f) else BoneWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
