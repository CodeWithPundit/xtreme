package com.xtremeiptv.core.network.response

import com.google.gson.annotations.SerializedName

data class MACHandshakeResponse(
    @SerializedName("js") val js: String?,
    @SerializedName("error") val error: String? = null
)

data class MACChannelResponse(
    val id: Int,
    val name: String,
    val number: String,
    val logo: String?,
    @SerializedName("epg_id") val epgId: String?,
    val archive: Int,
    @SerializedName("archive_url") val archiveUrl: String?
)

data class MACEpgResponse(
    val id: Int,
    @SerializedName("ch_id") val channelId: Int,
    val name: String,
    val descr: String?,
    @SerializedName("start_timestamp") val startTimestamp: Long,
    @SerializedName("stop_timestamp") val stopTimestamp: Long,
    @SerializedName("archive_uri") val archiveUri: String?
)
