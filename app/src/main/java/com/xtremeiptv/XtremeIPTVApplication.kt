package com.xtremeiptv

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.xtremeiptv.core.common.analytics.AnalyticsManager
import com.xtremeiptv.core.common.analytics.CrashReporter
import com.xtremeiptv.core.common.di.ApplicationScope
import com.xtremeiptv.core.common.manager.PreferenceManager
import com.xtremeiptv.core.common.manager.SessionManager
import com.xtremeiptv.core.common.monitor.NetworkMonitor
import com.xtremeiptv.service.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class XtremeIPTVApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    
    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Plant release tree for crash reporting
            Timber.plant(ReleaseTree())
        }
        
        // Initialize crash reporting
        crashReporter.initialize()
        
        // Initialize Ads
        MobileAds.initialize(this)
        
        // Start network monitoring
        applicationScope.launch {
            networkMonitor.startMonitoring()
        }
        
        // Initialize sync worker
        applicationScope.launch {
            if (preferenceManager.isAutoSyncEnabled()) {
                SyncWorker.startPeriodicSync()
            }
        }
        
        // Log app open
        analyticsManager.logAppOpen()
        
        // Check if first launch
        applicationScope.launch {
            if (preferenceManager.isFirstLaunch()) {
                handleFirstLaunch()
            }
        }
    }

    private suspend fun handleFirstLaunch() {
        // Set default preferences
        preferenceManager.setDefaultPreferences()
        
        // Create default directories
        createDefaultDirectories()
        
        // Mark first launch as complete
        preferenceManager.setFirstLaunchComplete()
    }

    private fun createDefaultDirectories() {
        val directories = listOf(
            "Downloads",
            "Recordings",
            "Cache"
        )
        
        directories.forEach { dirName ->
            val dir = getExternalFilesDir(dirName)
            if (dir != null && !dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                // UI is hidden, release resources
                Timber.d("UI hidden, releasing resources")
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Memory is critical, clear caches
                Timber.d("Memory critical, clearing caches")
                clearCaches()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.d("Low memory, clearing caches")
        clearCaches()
    }

    private fun clearCaches() {
        // Clear image cache
        coil.ImageLoader.getInstance(this).diskCache?.clear()
        
        // Clear network cache
        // implementation
    }

    class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Only log errors and warnings in release
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // Send to crash reporting service
                if (t != null) {
                    // Report exception
                }
            }
        }
    }
}
