package com.xtremeiptv.core.data.parser

import com.google.gson.Gson
import com.xtremeiptv.core.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MACPortalParser @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) : ProtocolParser {

    private var sessionId: String? = null
    private var cookies: Map<String, String> = emptyMap()

    override suspend fun parseLiveStreams(profile: Profile): Result<List<StreamModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as ProtocolConfig.MACPortal
                authenticate(config)
                fetchChannels(config)
            }
        }

    override suspend fun parseVOD(profile: Profile): Result<List<StreamModel>> =
        Result.success(emptyList()) // MAC portal typically doesn't have VOD

    override suspend fun parseSeries(profile: Profile): Result<List<StreamModel>> =
        Result.success(emptyList())

    override suspend fun parseEPG(profile: Profile): Result<List<EpgProgram>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as ProtocolConfig.MACPortal
                authenticate(config)
                fetchEPG(config)
            }
        }

    override suspend fun authenticate(profile: Profile): Result<Boolean> =
        runCatching {
            val config = profile.protocolConfig as ProtocolConfig.MACPortal
            authenticate(config)
            true
        }

    override suspend fun validateCredentials(profile: Profile): Result<Boolean> =
        runCatching {
            val config = profile.protocolConfig as ProtocolConfig.MACPortal
            authenticate(config)
            true
        }

    override fun getCatchupUrl(stream: StreamModel, programStartTime: Long): String? {
        return stream.url.replace("{start}", programStartTime.toString())
    }

    private suspend fun authenticate(config: ProtocolConfig.MACPortal) {
        val handshakeUrl = "${config.portalUrl}/portal/handshake.php"
        val handshakeRequest = Request.Builder()
            .url(handshakeUrl)
            .addHeader("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .apply {
                config.headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()

        okHttpClient.newCall(handshakeRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Handshake failed: ${response.code}")
            }

            // Extract cookies
            cookies = response.headers("Set-Cookie").associate { cookie ->
                val parts = cookie.split("=")
                parts[0] to parts[1].split(";").first()
            }
        }

        // Authenticate with MAC
        val authUrl = "${config.portalUrl}/portal/auth.php"
        val authRequest = Request.Builder()
            .url(authUrl)
            .addHeader("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
            .addHeader("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .post(emptyBody)
            .build()

        okHttpClient.newCall(authRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Authentication failed: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            val json = gson.fromJson(responseBody, Map::class.java)
            sessionId = json["js"]?.toString()
        }

        // Get server info
        val serverUrl = "${config.portalUrl}/portal/server/load.php"
        val serverRequest = Request.Builder()
            .url(serverUrl)
            .addHeader("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
            .addHeader("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36")
            .build()

        okHttpClient.newCall(serverRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Server info failed: ${response.code}")
            }
        }
    }

    private suspend fun fetchChannels(config: ProtocolConfig.MACPortal): List<StreamModel> {
        val channelsUrl = "${config.portalUrl}/portal/server/load.php?type=itv&action=get_all_channels"
        val request = Request.Builder()
            .url(channelsUrl)
            .addHeader("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
            .addHeader("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch channels: ${response.code}")
            }

            val json = response.body?.string() ?: throw Exception("Empty response")
            val channels = gson.fromJson(json, Array<MACChannel>::class.java)

            channels.map { channel ->
                StreamModel(
                    id = "mac_${channel.id}",
                    title = channel.name,
                    type = StreamType.LIVE,
                    url = "${config.portalUrl}/stream/${channel.id}",
                    thumbnailUrl = channel.logo,
                    logoUrl = channel.logo,
                    epgChannelId = channel.epg_id,
                    archive = ArchiveInfo(
                        isAvailable = channel.archive == 1,
                        catchupUrl = "${config.portalUrl}/archive/${channel.id}/{start}"
                    )
                )
            }.toList()
        }
    }

    private suspend fun fetchEPG(config: ProtocolConfig.MACPortal): List<EpgProgram> {
        val epgUrl = "${config.portalUrl}/portal/server/load.php?type=epg&action=get_all_programs"
        val request = Request.Builder()
            .url(epgUrl)
            .addHeader("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
            .addHeader("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/537.36")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch EPG: ${response.code}")
            }

            val json = response.body?.string() ?: throw Exception("Empty response")
            val programs = gson.fromJson(json, Array<MACEpgProgram>::class.java)

            programs.map { program ->
                EpgProgram(
                    id = program.id.toString(),
                    channelId = program.ch_id.toString(),
                    channelName = "",
                    title = program.name,
                    description = program.descr,
                    startTime = java.util.Date(program.start_timestamp * 1000),
                    endTime = java.util.Date(program.stop_timestamp * 1000),
                    isCatchupAvailable = program.archive_uri != null,
                    catchupUrl = program.archive_uri
                )
            }.toList()
        }
    }

    private val emptyBody = "".toRequestBody(null)
}

data class MACChannel(
    val id: Int,
    val name: String,
    val number: String,
    val logo: String?,
    val epg_id: String?,
    val archive: Int,
    val archive_url: String?
)

data class MACEpgProgram(
    val id: Int,
    val ch_id: Int,
    val name: String,
    val descr: String?,
    val start_timestamp: Long,
    val stop_timestamp: Long,
    val archive_uri: String?
)
