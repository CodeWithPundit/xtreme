package com.xtremeiptv.core.common.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsImpl @Inject constructor() : AnalyticsManager {
    
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val bundle = android.os.Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    override fun logScreenView(screenName: String) {
        val params = mapOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to screenName
        )
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }
    
    override fun logStreamPlay(streamId: String, streamType: String, duration: Long) {
        val params = mapOf(
            "stream_id" to streamId,
            "stream_type" to streamType,
            "content_type" to "video",
            "duration" to duration
        )
        logEvent("stream_play", params)
    }
    
    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
    
    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
