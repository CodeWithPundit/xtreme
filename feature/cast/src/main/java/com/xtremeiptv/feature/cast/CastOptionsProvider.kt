package com.xtremeiptv.feature.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val buttonData = listOf(
            MediaIntentReceiver.ACTION_SKIP_NEXT,
            MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
            MediaIntentReceiver.ACTION_STOP_CASTING
        )

        val compatButtonActionsIndices = NotificationOptions.getCompatActionIndices(
            buttonData
        )

        val notificationOptions = NotificationOptions.Builder()
            .setActions(buttonData, compatButtonActionsIndices)
            .setTargetActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()

        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()

        return CastOptions.Builder()
            .setReceiverApplicationId("YOUR_APP_ID") // Replace with your Cast app ID
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
