package com.xtremeiptv.core.data.repository

import com.xtremeiptv.core.domain.model.EpgProgram
import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.model.StreamType
import com.xtremeiptv.core.domain.repository.IStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val localStreamDao: LocalStreamDao,
    private val protocolParserFactory: ProtocolParserFactory,
    private val epgParser: EPGParser,
    private val networkMonitor: NetworkMonitor
) : IStreamRepository {
    
    override fun getLiveStreams(profileId: String): Flow<List<StreamModel>> =
        combine(
            localStreamDao.getStreamsByType(profileId, StreamType.LIVE),
            networkMonitor.isOnline
        ) { cachedStreams, isOnline ->
            if (cachedStreams.isNotEmpty() && !isOnline) {
                cachedStreams.map { it.toDomain() }
            } else {
                refreshLiveStreams(profileId)
                cachedStreams.map { it.toDomain() }
            }
        }
    
    override fun getMovies(profileId: String): Flow<List<StreamModel>> =
        localStreamDao.getStreamsByType(profileId, StreamType.MOVIE)
            .map { entities -> entities.map { it.toDomain() } }
    
    override fun getSeries(profileId: String): Flow<List<StreamModel>> =
        localStreamDao.getStreamsByType(profileId, StreamType.SERIES)
            .map { entities -> entities.map { it.toDomain() } }
    
    override suspend fun refreshLiveStreams(profileId: String): Result<Unit> = runCatching {
        val profile = getProfile(profileId) ?: return@runCatching Result.failure(Exception("Profile not found"))
        val parser = protocolParserFactory.createParser(profile.protocolType)
        
        parser.parseLiveStreams(profile).fold(
            onSuccess = { streams ->
                localStreamDao.insertStreams(streams.map { it.toEntity(profileId) })
            },
            onFailure = { error ->
                throw error
            }
        )
    }
    
    override fun searchStreams(
        profileId: String,
        query: String,
        type: StreamType?
    ): Flow<List<StreamModel>> =
        localStreamDao.searchStreams(profileId, query, type?.name)
            .map { entities -> entities.map { it.toDomain() } }
    
    override fun getStreamDetails(streamId: String): Flow<StreamModel?> =
        localStreamDao.getStream(streamId)
            .map { it?.toDomain() }
    
    override suspend fun updateStream(stream: StreamModel): Result<Unit> = runCatching {
        localStreamDao.updateStream(stream.toEntity())
    }
    
    override fun getEPGForChannel(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<EpgProgram>> = flow {
        emit(epgParser.getPrograms(channelId, startTime, endTime))
    }
    
    override suspend fun refreshEPG(profileId: String): Result<Unit> = runCatching {
        val profile = getProfile(profileId) ?: return@runCatching Result.failure(Exception("Profile not found"))
        val parser = protocolParserFactory.createParser(profile.protocolType)
        
        parser.parseEPG(profile).fold(
            onSuccess = { programs ->
                epgParser.savePrograms(programs)
            },
            onFailure = { error ->
                throw error
            }
        )
    }
}
