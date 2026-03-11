package com.xtremeiptv.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class StreamModel(
    val id: String,
    val title: String,
    val type: StreamType,
    val url: String,
    val thumbnailUrl: String? = null,
    val logoUrl: String? = null,
    val group: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val epgChannelId: String? = null,
    val epgChannelName: String? = null,
    val country: String? = null,
    val language: String? = null,
    val isAdult: Boolean = false,
    val isProtected: Boolean = false,
    val archive: ArchiveInfo? = null,
    val streams: List<QualityStream>? = null,
    val subtitles: List<SubtitleTrack>? = null,
    val audioTracks: List<AudioTrack>? = null,
    val metaData: StreamMetaData? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

enum class StreamType {
    LIVE,
    MOVIE,
    SERIES,
    SERIES_EPISODE
}

@Parcelize
data class QualityStream(
    val name: String,
    val url: String,
    val bitrate: Int? = null,
    val codec: String? = null,
    val width: Int? = null,
    val height: Int? = null
) : Parcelable

@Parcelize
data class SubtitleTrack(
    val id: String,
    val language: String,
    val url: String,
    val format: String,
    val isDefault: Boolean = false
) : Parcelable

@Parcelize
data class AudioTrack(
    val id: String,
    val language: String,
    val url: String? = null,
    val isDefault: Boolean = false
) : Parcelable

@Parcelize
data class ArchiveInfo(
    val isAvailable: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val maxDays: Int? = null
) : Parcelable

@Parcelize
data class StreamMetaData(
    val description: String? = null,
    val year: Int? = null,
    val rating: Double? = null,
    val duration: Int? = null,
    val director: String? = null,
    val cast: List<String>? = null,
    val genres: List<String>? = null
) : Parcelable
