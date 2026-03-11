package com.xtremeiptv.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val avatarColor: Int? = null,
    val pin: String? = null,
    val protocolType: ProtocolType,
    val protocolConfig: ProtocolConfig,
    val isActive: Boolean = true,
    val lastUsed: Long? = null,
    val createdAt: Long,
    val settings: ProfileSettings = ProfileSettings()
) : Parcelable

enum class ProtocolType {
    M3U,
    XTREAM_CODES,
    STALKER_PORTAL,
    MAC_PORTAL
}

sealed class ProtocolConfig : Parcelable {
    @Parcelize
    data class M3U(
        val url: String? = null,
        val localPath: String? = null,
        val epgUrl: String? = null,
        val userAgent: String? = null
    ) : ProtocolConfig()

    @Parcelize
    data class XtreamCodes(
        val serverUrl: String,
        val username: String,
        val password: String,
        val useHttps: Boolean = true
    ) : ProtocolConfig()

    @Parcelize
    data class StalkerPortal(
        val portalUrl: String,
        val macAddress: String,
        val username: String? = null,
        val password: String? = null,
        val token: String? = null
    ) : ProtocolConfig()

    @Parcelize
    data class MACPortal(
        val portalUrl: String,
        val macAddress: String,
        val headers: Map<String, String> = emptyMap()
    ) : ProtocolConfig()
}

@Parcelize
data class ProfileSettings(
    val autoRefreshPlaylist: Boolean = true,
    val refreshIntervalHours: Int = 24,
    val parentalControlEnabled: Boolean = false,
    val defaultVideoQuality: String = "auto",
    val enableSubtitles: Boolean = true,
    val preferredSubtitleLanguage: String? = null,
    val preferredAudioLanguage: String? = null,
    val rememberHistory: Boolean = true
) : Parcelable
