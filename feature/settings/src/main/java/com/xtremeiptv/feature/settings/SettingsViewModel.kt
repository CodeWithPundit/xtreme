package com.xtremeiptv.feature.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.core.common.manager.PreferenceManager
import com.xtremeiptv.core.common.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val preferenceManager: PreferenceManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings()
        calculateStorage()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                autoPlay = preferenceManager.isAutoPlayEnabled().value,
                backgroundPlay = preferenceManager.isBackgroundPlayEnabled().value,
                bufferSize = preferenceManager.getBufferSize().value.toFloat(),
                defaultQuality = preferenceManager.getDefaultQuality().value,
                downloadOnlyWifi = preferenceManager.isDownloadOnlyWifi().value,
                downloadQuality = preferenceManager.getDownloadQuality().value,
                autoRefreshEpg = preferenceManager.isAutoRefreshEpg().value,
                epgRetentionDays = preferenceManager.getEpgRetentionDays().value,
                parentalControlsEnabled = preferenceManager.isParentalControlsEnabled().value,
                contentRestriction = preferenceManager.getContentRestriction().value,
                useMobileData = preferenceManager.isUseMobileData().value,
                userAgent = preferenceManager.getUserAgent().value,
                proxyUrl = preferenceManager.getProxyUrl().value,
                appVersion = getAppVersion()
            )
        }
    }

    private fun calculateStorage() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val downloadsDir = FileUtils.getDownloadsDirectory(context)
            val recordingsDir = FileUtils.getRecordingsDirectory(context)
            
            var totalSize = 0L
            if (downloadsDir.exists()) {
                totalSize += downloadsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }
            if (recordingsDir.exists()) {
                totalSize += recordingsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }
            
            val stat = StatFs(context.filesDir.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            
            _uiState.value = _uiState.value.copy(
                usedSpace = FileUtils.getFileSizeString(totalSize),
                totalSpace = FileUtils.getFileSizeString(availableBytes + totalSize)
            )
        }
    }

    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setAutoPlayEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoPlay = enabled)
        }
    }

    fun setBackgroundPlay(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBackgroundPlayEnabled(enabled)
            _uiState.value = _uiState.value.copy(backgroundPlay = enabled)
        }
    }

    fun setBufferSize(size: Float) {
        viewModelScope.launch {
            preferenceManager.setBufferSize(size.toInt())
            _uiState.value = _uiState.value.copy(bufferSize = size)
        }
    }

    fun setDefaultQuality(quality: String) {
        viewModelScope.launch {
            preferenceManager.setDefaultQuality(quality)
            _uiState.value = _uiState.value.copy(defaultQuality = quality)
        }
    }

    fun setDownloadOnlyWifi(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDownloadOnlyWifi(enabled)
            _uiState.value = _uiState.value.copy(downloadOnlyWifi = enabled)
        }
    }

    fun setDownloadQuality(quality: String) {
        viewModelScope.launch {
            preferenceManager.setDownloadQuality(quality)
            _uiState.value = _uiState.value.copy(downloadQuality = quality)
        }
    }

    fun setAutoRefreshEpg(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setAutoRefreshEpg(enabled)
            _uiState.value = _uiState.value.copy(autoRefreshEpg = enabled)
        }
    }

    fun setEpgRetentionDays(days: Int) {
        viewModelScope.launch {
            preferenceManager.setEpgRetentionDays(days)
            _uiState.value = _uiState.value.copy(epgRetentionDays = days)
        }
    }

    fun setParentalControlsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setParentalControlsEnabled(enabled)
            _uiState.value = _uiState.value.copy(parentalControlsEnabled = enabled)
        }
    }

    fun setContentRestriction(restriction: String) {
        viewModelScope.launch {
            preferenceManager.setContentRestriction(restriction)
            _uiState.value = _uiState.value.copy(contentRestriction = restriction)
        }
    }

    fun setUseMobileData(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setUseMobileData(enabled)
            _uiState.value = _uiState.value.copy(useMobileData = enabled)
        }
    }

    fun setUserAgent(agent: String) {
        viewModelScope.launch {
            preferenceManager.setUserAgent(agent)
            _uiState.value = _uiState.value.copy(userAgent = agent)
        }
    }

    fun setProxyUrl(url: String) {
        viewModelScope.launch {
            preferenceManager.setProxyUrl(url)
            _uiState.value = _uiState.value.copy(proxyUrl = url)
        }
    }

    fun changeStorageLocation() {
        // Implement storage location picker
    }

    fun changePin() {
        // Implement PIN change dialog
    }

    fun openTermsOfService() {
        openUrl("https://www.xtremeiptv.com/terms")
    }

    fun openPrivacyPolicy() {
        openUrl("https://www.xtremeiptv.com/privacy")
    }

    fun openLicenses() {
        openUrl("https://www.xtremeiptv.com/licenses")
    }

    fun checkForUpdates() {
        // Implement update check
    }

    fun clearAllCache() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val cacheDir = context.cacheDir
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            calculateStorage()
        }
    }

    fun clearDownloadHistory() {
        viewModelScope.launch {
            // Implement download history clear
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            preferenceManager.resetAllSettings()
            loadSettings()
        }
    }

    fun deleteAllProfiles() {
        viewModelScope.launch {
            // Implement profile deletion
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private fun getAppVersion(): String {
        val context = getApplication<Application>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName ?: "1.0.0"
    }
}

data class SettingsUiState(
    val autoPlay: Boolean = true,
    val backgroundPlay: Boolean = true,
    val bufferSize: Float = 5f,
    val defaultQuality: String = "Auto",
    val downloadOnlyWifi: Boolean = true,
    val downloadQuality: String = "Same as streaming",
    val autoRefreshEpg: Boolean = true,
    val epgRetentionDays: Int = 7,
    val parentalControlsEnabled: Boolean = false,
    val contentRestriction: String = "All",
    val useMobileData: Boolean = true,
    val userAgent: String = "",
    val proxyUrl: String = "",
    val appVersion: String = "1.0.0",
    val usedSpace: String = "0 MB",
    val totalSpace: String = "0 MB"
)
