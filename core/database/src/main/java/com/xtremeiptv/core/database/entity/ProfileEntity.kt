package com.xtremeiptv.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.domain.model.ProfileSettings
import com.xtremeiptv.core.domain.model.ProtocolConfig
import com.xtremeiptv.core.domain.model.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    
    @ColumnInfo(name = "avatar_color")
    val avatarColor: Int?,
    
    @ColumnInfo(name = "pin_hash")
    val pinHash: String?,
    
    @ColumnInfo(name = "protocol_type")
    val protocolType: String,
    
    @ColumnInfo(name = "protocol_config_json")
    val protocolConfigJson: String,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "settings_json")
    val settingsJson: String
)

fun ProfileEntity.toDomain(): Profile {
    val protocolType = ProtocolType.valueOf(protocolType)
    val protocolConfig = Json.decodeFromString<ProtocolConfig>(protocolConfigJson)
    val settings = Json.decodeFromString<ProfileSettings>(settingsJson)
    
    return Profile(
        id = profileId,
        name = name,
        avatarUrl = avatarUrl,
        avatarColor = avatarColor,
        pin = pinHash,
        protocolType = protocolType,
        protocolConfig = protocolConfig,
        isActive = isActive,
        lastUsed = lastUsed,
        createdAt = createdAt,
        settings = settings
    )
}

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        profileId = id,
        name = name,
        avatarUrl = avatarUrl,
        avatarColor = avatarColor,
        pinHash = pin,
        protocolType = protocolType.name,
        protocolConfigJson = Json.encodeToString(protocolConfig),
        isActive = isActive,
        lastUsed = lastUsed,
        createdAt = createdAt,
        settingsJson = Json.encodeToString(settings)
    )
}
