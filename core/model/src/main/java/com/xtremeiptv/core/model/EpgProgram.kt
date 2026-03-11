package com.xtremeiptv.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class EpgProgram(
    val id: String,
    val channelId: String,
    val channelName: String,
    val title: String,
    val description: String? = null,
    val startTime: Date,
    val endTime: Date,
    val category: String? = null,
    val isCatchupAvailable: Boolean = false,
    val catchupUrl: String? = null,
    val catchupSource: String? = null,
    val catchupDays: Int? = null,
    val rating: String? = null,
    val episodeTitle: String? = null,
    val episodeNumber: Int? = null,
    val seasonNumber: Int? = null,
    val icon: String? = null
) : Parcelable {
    val duration: Long
        get() = (endTime.time - startTime.time) / 1000
    
    val isCurrentlyPlaying: Boolean
        get() {
            val now = Date()
            return now.after(startTime) && now.before(endTime)
        }
    
    val progress: Float
        get() {
            if (!isCurrentlyPlaying) return 0f
            val total = endTime.time - startTime.time
            val elapsed = Date().time - startTime.time
            return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
}
