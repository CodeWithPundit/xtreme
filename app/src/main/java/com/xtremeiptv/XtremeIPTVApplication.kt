package com.xtremeiptv

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.xtremeiptv.core.common.di.ApplicationScope
import com.xtremeiptv.core.common.manager.PreferenceManager
import com.xtremeiptv.core.common.monitor.NetworkMonitor
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
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize Ads
        MobileAds.initialize(this)
        
        // Check if disclaimer accepted
        applicationScope.launch {
            if (!preferenceManager.isDisclaimerAccepted()) {
                // Navigate to disclaimer screen will be handled by MainActivity
            }
        }
        
        // Start network monitoring
        applicationScope.launch {
            networkMonitor.startMonitoring()
        }
        
        // Initialize crash reporting (if using Firebase)
        if (!BuildConfig.DEBUG) {
            // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
