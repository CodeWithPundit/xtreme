package com.xtremeiptv.core.common.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporterImpl @Inject constructor() : CrashReporter {
    
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics
    
    override fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
    
    override fun logMessage(message: String) {
        crashlytics.log(message)
    }
    
    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
    
    override fun logNonFatalException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
