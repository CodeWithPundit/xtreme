package com.xtremeiptv.core.network.response

import com.google.gson.annotations.SerializedName

data class StalkerHandshakeResponse(
    val status: String,
    val token: String?,
    @SerializedName("error") val error: String? = null
)

data class StalkerAuthResponse(
    val status: String,
    @SerializedName("error") val error: String? = null
)

data class StalkerProfileResponse(
    val id: String,
    val name: String,
    val mac: String,
    val status: String,
    @SerializedName("expire_date") val expireDate: String
)

data class StalkerChannelResponse(
    val id: String,
    val name: String,
    val number: String,
    val logo: String?,
    val url: String,
    @SerializedName("archive") val archive: StalkerArchiveResponse?,
    @SerializedName("epg_id") val epgId: String?,
    val cmd: String
)

data class StalkerArchiveResponse(
    @SerializedName("var") val enabled: Int,
    @SerializedName("archive_url") val archiveUrl: String?
)

data class StalkerVodResponse(
    val id: String,
    val name: String,
    val description: String,
    val logo: String?,
    @SerializedName("screenshot_uri") val screenshotUri: String?,
    val year: String?,
    val director: String?,
    val cast: String?,
    val genres: String?,
    val rating: String?,
    val url: String
)

data class StalkerEpgResponse(
    val id: String,
    @SerializedName("ch_id") val channelId: String,
    val name: String,
    val desc: String?,
    @SerializedName("start_timestamp") val startTimestamp: Long,
    @SerializedName("stop_timestamp") val stopTimestamp: Long,
    val url: String?,
    @SerializedName("archive_uri") val archiveUri: String?
)
