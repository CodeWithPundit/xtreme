package com.xtremeiptv.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.model.StreamType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(
    tableName = "streams",
    indices = [
        Index(value = ["profile_id", "stream_type", "category_id"]),
        Index(value = ["profile_id", "title"], unique = true)
    ]
)
data class StreamEntity(
    @PrimaryKey
    @ColumnInfo(name = "stream_id")
    val streamId: String,
    
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "stream_type")
    val streamType: String,
    
    @ColumnInfo(name = "url")
    val url: String,
    
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    
    @ColumnInfo(name = "group_name")
    val groupName: String?,
    
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    
    @ColumnInfo(name = "category_name")
    val categoryName: String?,
    
    @ColumnInfo(name = "epg_channel_id")
    val epgChannelId: String?,
    
    @ColumnInfo(name = "epg_channel_name")
    val epgChannelName: String?,
    
    @ColumnInfo(name = "country")
    val country: String?,
    
    @ColumnInfo(name = "language")
    val language: String?,
    
    @ColumnInfo(name = "is_adult")
    val isAdult: Boolean,
    
    @ColumnInfo(name = "is_protected")
    val isProtected: Boolean,
    
    @ColumnInfo(name = "archive_info_json")
    val archiveInfoJson: String?,
    
    @ColumnInfo(name = "quality_streams_json")
    val qualityStreamsJson: String?,
    
    @ColumnInfo(name = "subtitles_json")
    val subtitlesJson: String?,
    
    @ColumnInfo(name = "audio_tracks_json")
    val audioTracksJson: String?,
    
    @ColumnInfo(name = "meta_data_json")
    val metaDataJson: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

fun StreamEntity.toDomain(): StreamModel {
    return StreamModel(
        id = streamId,
        title = title,
        type = StreamType.valueOf(streamType),
        url = url,
        thumbnailUrl = thumbnailUrl,
        logoUrl = logoUrl,
        group = groupName,
        categoryId = categoryId,
        categoryName = categoryName,
        epgChannelId = epgChannelId,
        epgChannelName = epgChannelName,
        country = country,
        language = language,
        isAdult = isAdult,
        isProtected = isProtected,
        archive = archiveInfoJson?.let { Json.decodeFromString(it) },
        streams = qualityStreamsJson?.let { Json.decodeFromString(it) },
        subtitles = subtitlesJson?.let { Json.decodeFromString(it) },
        audioTracks = audioTracksJson?.let { Json.decodeFromString(it) },
        metaData = metaDataJson?.let { Json.decodeFromString(it) },
        createdAt = java.util.Date(createdAt),
        updatedAt = java.util.Date(updatedAt)
    )
}

fun StreamModel.toEntity(profileId: String): StreamEntity {
    return StreamEntity(
        streamId = id,
        profileId = profileId,
        title = title,
        streamType = type.name,
        url = url,
        thumbnailUrl = thumbnailUrl,
        logoUrl = logoUrl,
        groupName = group,
        categoryId = categoryId,
        categoryName = categoryName,
        epgChannelId = epgChannelId,
        epgChannelName = epgChannelName,
        country = country,
        language = language,
        isAdult = isAdult,
        isProtected = isProtected,
        archiveInfoJson = archive?.let { Json.encodeToString(it) },
        qualityStreamsJson = streams?.let { Json.encodeToString(it) },
        subtitlesJson = subtitles?.let { Json.encodeToString(it) },
        audioTracksJson = audioTracks?.let { Json.encodeToString(it) },
        metaDataJson = metaData?.let { Json.encodeToString(it) },
        createdAt = createdAt.time,
        updatedAt = updatedAt.time
    )
}
