package com.xtremeiptv.core.network.response

import com.google.gson.annotations.SerializedName

data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: XtreamUserInfo,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo
)

data class XtreamUserInfo(
    val username: String,
    val password: String,
    @SerializedName("auth") val auth: Int,
    val status: String,
    @SerializedName("exp_date") val expDate: String,
    @SerializedName("is_trial") val isTrial: String,
    @SerializedName("active_cons") val activeCons: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("max_connections") val maxConnections: String,
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String>
)

data class XtreamServerInfo(
    val url: String,
    val port: String,
    @SerializedName("https_port") val httpsPort: String,
    @SerializedName("server_protocol") val serverProtocol: String,
    @SerializedName("rtmp_port") val rtmpPort: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timestamp_now") val timestampNow: Long,
    @SerializedName("time_now") val timeNow: String
)

data class XtreamCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int
)

data class XtreamLiveStream(
    val num: Int,
    val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int
)

data class XtreamVodStream(
    val num: Int,
    val name: String,
    val title: String,
    val year: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    val rating: String,
    @SerializedName("rating_5based") val rating5based: Double,
    val added: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("container_extension") val containerExtension: String,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?
)

data class XtreamSeries(
    val num: Int,
    val name: String,
    val title: String,
    val year: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("series_id") val seriesId: Int,
    val cover: String,
    val plot: String,
    val cast: String,
    val director: String,
    val genre: String,
    @SerializedName("releaseDate") val releaseDate: String,
    @SerializedName("last_modified") val lastModified: String,
    val rating: String,
    @SerializedName("rating_5based") val rating5based: Double,
    @SerializedName("backdrop_path") val backdropPath: List<String>,
    @SerializedName("youtube_trailer") val youtubeTrailer: String,
    @SerializedName("episode_run_time") val episodeRunTime: String,
    val category_id: String
)

data class XtreamSeriesInfo(
    val seasons: List<XtreamSeason>
)

data class XtreamSeason(
    val name: String,
    @SerializedName("season_number") val seasonNumber: Int,
    val episodes: List<XtreamEpisode>
)

data class XtreamEpisode(
    val id: String,
    @SerializedName("episode_num") val episodeNum: Int,
    val title: String,
    @SerializedName("container_extension") val containerExtension: String,
    val info: XtreamEpisodeInfo,
    @SerializedName("custom_sid") val customSid: String?,
    val added: String,
    @SerializedName("season") val season: Int,
    @SerializedName("direct_source") val directSource: String?
)

data class XtreamEpisodeInfo(
    val plot: String,
    @SerializedName("duration_secs") val durationSecs: Int?,
    val duration: String,
    val movie_image: String?,
    val bitrate: Int?,
    val rating: String?,
    @SerializedName("season") val season: Int?,
    @SerializedName("episode") val episode: Int?
)

data class XtreamVodInfo(
    val info: XtreamVodDetails,
    val movie_data: XtreamMovieData
)

data class XtreamVodDetails(
    val name: String,
    @SerializedName("cover_big") val coverBig: String,
    val movie_image: String,
    @SerializedName("releasedate") val releaseDate: String,
    @SerializedName("episode_run_time") val episodeRunTime: String,
    val youtube_trailer: String,
    val director: String,
    val cast: String,
    val plot: String,
    val genre: String,
    val rating: String,
    @SerializedName("rating_5based") val rating5based: Double,
    @SerializedName("backdrop_path") val backdropPath: List<String>
)

data class XtreamMovieData(
    @SerializedName("stream_id") val streamId: Int,
    val name: String,
    val title: String,
    val year: String,
    @SerializedName("added") val added: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("container_extension") val containerExtension: String,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?
)

data class XtreamEpgProgram(
    val id: String,
    @SerializedName("epg_id") val epgId: String,
    val title: String,
    val lang: String,
    val start: String,
    val end: String,
    val description: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("start_timestamp") val startTimestamp: Long,
    @SerializedName("stop_timestamp") val stopTimestamp: Long,
    @SerializedName("has_archive") val hasArchive: Int
)
