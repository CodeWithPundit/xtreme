package com.xtremeiptv.core.data.parser

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xtremeiptv.core.domain.model.*
import com.xtremeiptv.core.network.api.StalkerPortalApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StalkerPortalParser @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) : ProtocolParser {

    private val jsonMediaType = "application/json".toMediaType()
    private var sessionCookie: String? = null
    private var token: String? = null

    override suspend fun parseLiveStreams(profile: Profile): Result<List<StreamModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as ProtocolConfig.StalkerPortal
                ensureAuthenticated(profile)
                
                val channels = fetchChannels(config)
                channels.map { it.toStreamModel() }
            }
        }

    override suspend fun parseVOD(profile: Profile): Result<List<StreamModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as ProtocolConfig.StalkerPortal
                ensureAuthenticated(profile)
                
                val vod = fetchVod(config)
                vod.map { it.toStreamModel() }
            }
        }

    override suspend fun parseSeries(profile: Profile): Result<List<StreamModel>> =
        Result.success(emptyList()) // Stalker doesn't have native series support

    override suspend fun parseEPG(profile: Profile): Result<List<EpgProgram>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as ProtocolConfig.StalkerPortal
                ensureAuthenticated(profile)
                
                fetchEpg(config)
            }
        }

    override suspend fun authenticate(profile: Profile): Result<Boolean> =
        runCatching {
            val config = profile.protocolConfig as ProtocolConfig.StalkerPortal
            performHandshake(config)
            true
        }

    override suspend fun validateCredentials(profile: Profile): Result<Boolean> =
        runCatching {
            val config = profile.protocolConfig as ProtocolConfig.StalkerPortal
            performHandshake(config)
            true
        }

    override fun getCatchupUrl(stream: StreamModel, programStartTime: Long): String? {
        return stream.archive?.catchupUrl?.replace("{utc}", programStartTime.toString())
    }

    private suspend fun ensureAuthenticated(config: ProtocolConfig.StalkerPortal) {
        if (token == null || sessionCookie == null) {
            performHandshake(config)
        }
    }

    private suspend fun performHandshake(config: ProtocolConfig.StalkerPortal) {
        // Step 1: Get token
        val handshakeUrl = "${config.portalUrl}/stalker_portal/server/load.php?type=stb&action=handshake"
        val handshakeRequest = Request.Builder()
            .url(handshakeUrl)
            .addHeader("Authorization", "Bearer ${config.token ?: ""}")
            .addHeader("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C)")
            .build()

        okHttpClient.newCall(handshakeRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Handshake failed: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            token = json.get("token")?.asString
            
            // Save session cookie
            response.headers("Set-Cookie").firstOrNull()?.let { cookie ->
                sessionCookie = cookie.split(";").firstOrNull()
            }
        }

        // Step 2: Authenticate with MAC
        val authUrl = "${config.portalUrl}/stalker_portal/server/load.php?type=stb&action=do_auth"
        val authBody = gson.toJson(mapOf(
            "login" to (config.username ?: ""),
            "password" to (config.password ?: ""),
            "mac" to config.macAddress
        )).toRequestBody(jsonMediaType)

        val authRequest = Request.Builder()
            .url(authUrl)
            .addHeader("Cookie", sessionCookie ?: "")
            .addHeader("Authorization", "Bearer $token")
            .post(authBody)
            .build()

        okHttpClient.newCall(authRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Authentication failed: ${response.code}")
            }
        }

        // Step 3: Get profile
        val profileUrl = "${config.portalUrl}/stalker_portal/server/load.php?type=stb&action=get_profile"
        val profileRequest = Request.Builder()
            .url(profileUrl)
            .addHeader("Cookie", sessionCookie ?: "")
            .addHeader("Authorization", "Bearer $token")
            .build()

        okHttpClient.newCall(profileRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Get profile failed: ${response.code}")
            }
        }
    }

    private suspend fun fetchChannels(config: ProtocolConfig.StalkerPortal): List<StalkerChannel> {
        val url = "${config.portalUrl}/stalker_portal/server/load.php?type=itv&action=get_all_channels"
        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", sessionCookie ?: "")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch channels: ${response.code}")
            }
            val json = response.body?.string() ?: throw Exception("Empty response")
            gson.fromJson(json, Array<StalkerChannel>::class.java).toList()
        }
    }

    private suspend fun fetchVod(config: ProtocolConfig.StalkerPortal): List<StalkerVod> {
        val url = "${config.portalUrl}/stalker_portal/server/load.php?type=vod&action=get_ordered_list"
        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", sessionCookie ?: "")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch VOD: ${response.code}")
            }
            val json = response.body?.string() ?: throw Exception("Empty response")
            gson.fromJson(json, Array<StalkerVod>::class.java).toList()
        }
    }

    private suspend fun fetchEpg(config: ProtocolConfig.StalkerPortal): List<EpgProgram> {
        val url = "${config.portalUrl}/stalker_portal/server/load.php?type=epg&action=get_all_programs"
        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", sessionCookie ?: "")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch EPG: ${response.code}")
            }
            val json = response.body?.string() ?: throw Exception("Empty response")
            gson.fromJson(json, Array<StalkerEpgProgram>::class.java).toList()
                .map { it.toEpgProgram() }
        }
    }
}

data class StalkerChannel(
    val id: String,
    val name: String,
    val number: String,
    val logo: String?,
    val url: String,
    val archive: StalkerArchive?,
    val epg_id: String?,
    val cmd: String
)

data class StalkerArchive(
    val `var`: Int,
    val archive_url: String?
)

data class StalkerVod(
    val id: String,
    val name: String,
    val description: String,
    val logo: String?,
    val screenshot_uri: String?,
    val year: String?,
    val director: String?,
    val cast: String?,
    val genres: String?,
    val rating: String?,
    val url: String
)

data class StalkerEpgProgram(
    val id: String,
    val ch_id: String,
    val name: String,
    val desc: String?,
    val start_timestamp: Long,
    val stop_timestamp: Long,
    val url: String?,
    val archive_uri: String?
)

fun StalkerChannel.toStreamModel(): StreamModel {
    return StreamModel(
        id = "stalker_$id",
        title = name,
        type = StreamType.LIVE,
        url = cmd,
        thumbnailUrl = logo,
        logoUrl = logo,
        epgChannelId = epg_id,
        archive = archive?.let {
            ArchiveInfo(
                isAvailable = it.`var` == 1,
                catchupUrl = it.archive_url
            )
        },
        metaData = StreamMetaData(
            description = "Channel $number"
        )
    )
}

fun StalkerVod.toStreamModel(): StreamModel {
    return StreamModel(
        id = "stalker_vod_$id",
        title = name,
        type = StreamType.MOVIE,
        url = url,
        thumbnailUrl = screenshot_uri ?: logo,
        logoUrl = logo,
        metaData = StreamMetaData(
            description = description,
            year = year?.toIntOrNull(),
            rating = rating?.toDoubleOrNull(),
            director = director,
            cast = cast?.split(", "),
            genres = genres?.split(", ")
        )
    )
}

fun StalkerEpgProgram.toEpgProgram(): EpgProgram {
    return EpgProgram(
        id = id,
        channelId = ch_id,
        channelName = "",
        title = name,
        description = desc,
        startTime = java.util.Date(start_timestamp * 1000),
        endTime = java.util.Date(stop_timestamp * 1000),
        isCatchupAvailable = archive_uri != null,
        catchupUrl = archive_uri
    )
}
