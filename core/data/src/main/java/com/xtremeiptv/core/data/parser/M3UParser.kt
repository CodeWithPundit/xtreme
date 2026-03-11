package com.xtremeiptv.core.data.parser

import com.xtremeiptv.core.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor(
    private val okHttpClient: OkHttpClient
) : ProtocolParser {
    
    private val extInfPattern = Pattern.compile(
        "#EXTINF:(-?\\d+?)(?:,(.+?))?(?:\\s+(.+?))?"
    )
    
    private val attributePattern = Pattern.compile(
        "([a-zA-Z0-9_-]+)=\"([^\"]*)\""
    )
    
    override suspend fun parseLiveStreams(profile: Profile): Result<List<StreamModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = when (val config = profile.protocolConfig) {
                    is ProtocolConfig.M3U -> {
                        if (config.url != null) {
                            downloadPlaylist(config.url, config.userAgent)
                        } else if (config.localPath != null) {
                            File(config.localPath).readText()
                        } else {
                            throw IllegalArgumentException("No URL or local path provided")
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid protocol config for M3U parser")
                }
                
                parseM3UContent(content, StreamType.LIVE)
            }
        }
    
    override suspend fun parseVOD(profile: Profile): Result<List<StreamModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = when (val config = profile.protocolConfig) {
                    is ProtocolConfig.M3U -> {
                        if (config.url != null) {
                            downloadPlaylist(config.url, config.userAgent)
                        } else {
                            emptyList()
                        }
                    }
                    else -> emptyList()
                }
                
                parseM3UContent(content as String, StreamType.MOVIE)
            }
        }
    
    override suspend fun parseSeries(profile: Profile): Result<List<StreamModel>> =
        Result.success(emptyList()) // M3U doesn't have series structure
    
    override suspend fun parseEPG(profile: Profile): Result<List<EpgProgram>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = profile.protocolConfig as? ProtocolConfig.M3U
                    ?: return@withContext Result.success(emptyList())
                
                val epgUrl = config.epgUrl ?: return@withContext Result.success(emptyList())
                val content = downloadPlaylist(epgUrl, config.userAgent)
                
                parseEPGContent(content)
            }
        }
    
    override suspend fun authenticate(profile: Profile): Result<Boolean> =
        Result.success(true) // M3U doesn't require authentication
    
    override suspend fun validateCredentials(profile: Profile): Result<Boolean> =
        runCatching {
            val config = profile.protocolConfig as ProtocolConfig.M3U
            if (config.url != null) {
                val response = okHttpClient.newCall(
                    Request.Builder()
                        .url(config.url)
                        .head()
                        .build()
                ).execute()
                
                response.isSuccessful
            } else {
                config.localPath?.let { File(it).exists() } ?: false
            }
        }
    
    override fun getCatchupUrl(stream: StreamModel, programStartTime: Long): String? {
        return stream.archive?.catchupUrl?.replace("{start}", programStartTime.toString())
    }
    
    private suspend fun downloadPlaylist(url: String, userAgent: String?): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .apply {
                    userAgent?.let { addHeader("User-Agent", it) }
                }
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to download playlist: ${response.code}")
                }
                response.body?.string() ?: throw Exception("Empty response body")
            }
        }
    
    private fun parseM3UContent(content: String, type: StreamType): List<StreamModel> {
        val streams = mutableListOf<StreamModel>()
        val reader = BufferedReader(StringReader(content))
        var line: String?
        var currentExtInf: Map<String, String>? = null
        
        while (reader.readLine().also { line = it } != null) {
            line = line?.trim()
            
            when {
                line?.startsWith("#EXTINF:") == true -> {
                    currentExtInf = parseExtInf(line)
                }
                line?.startsWith("#EXTGRP:") == true -> {
                    // Group handling
                }
                !line.isNullOrEmpty() && !line.startsWith("#") -> {
                    currentExtInf?.let { attributes ->
                        streams.add(createStreamFromAttributes(attributes, line!!, type))
                    }
                    currentExtInf = null
                }
            }
        }
        
        return streams
    }
    
    private fun parseExtInf(line: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        
        val matcher = extInfPattern.matcher(line)
        if (matcher.find()) {
            attributes["duration"] = matcher.group(1) ?: "-1"
            attributes["title"] = matcher.group(2) ?: ""
            
            val attributesPart = matcher.group(3) ?: ""
            val attrMatcher = attributePattern.matcher(attributesPart)
            
            while (attrMatcher.find()) {
                attributes[attrMatcher.group(1)] = attrMatcher.group(2)
            }
        }
        
        return attributes
    }
    
    private fun createStreamFromAttributes(
        attributes: Map<String, String>,
        url: String,
        type: StreamType
    ): StreamModel {
        return StreamModel(
            id = "m3u_${url.hashCode()}_${System.currentTimeMillis()}",
            title = attributes["title"] ?: "Unknown",
            type = type,
            url = url,
            thumbnailUrl = attributes["tvg-logo"],
            logoUrl = attributes["tvg-logo"],
            group = attributes["group-title"],
            epgChannelId = attributes["tvg-id"],
            epgChannelName = attributes["tvg-name"],
            archive = ArchiveInfo(
                isAvailable = attributes.containsKey("catchup"),
                catchupSource = attributes["catchup"],
                catchupDays = attributes["catchup-days"]?.toIntOrNull(),
                startTime = attributes["catchup-start"]?.toLongOrNull(),
                endTime = attributes["catchup-end"]?.toLongOrNull()
            )
        )
    }
    
    private fun parseEPGContent(content: String): List<EpgProgram> {
        // XMLTV parser implementation
        return emptyList() // Placeholder
    }
}
