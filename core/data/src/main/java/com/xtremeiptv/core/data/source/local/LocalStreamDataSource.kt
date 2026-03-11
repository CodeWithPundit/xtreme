package com.xtremeiptv.core.data.source.local

import com.xtremeiptv.core.database.dao.StreamDao
import com.xtremeiptv.core.database.entity.StreamEntity
import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.model.StreamType
import com.xtremeiptv.core.data.source.StreamDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStreamDataSource @Inject constructor(
    private val streamDao: StreamDao
) : StreamDataSource {

    override fun getStreams(profileId: String, type: StreamType?): Flow<List<StreamModel>> =
        if (type != null) {
            streamDao.getStreamsByType(profileId, type.name).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            streamDao.getStreamsByProfile(profileId).map { entities ->
                entities.map { it.toDomain() }
            }
        }

    override fun getStream(streamId: String): Flow<StreamModel?> =
        streamDao.getStream(streamId).map { it?.toDomain() }

    override fun searchStreams(
        profileId: String,
        query: String,
        type: StreamType?
    ): Flow<List<StreamModel>> =
        if (type != null) {
            streamDao.searchStreamsByType(profileId, query, type.name).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            streamDao.searchStreams(profileId, query).map { entities ->
                entities.map { it.toDomain() }
            }
        }

    override suspend fun insertStreams(streams: List<StreamModel>, profileId: String) {
        streamDao.insertStreams(streams.map { it.toEntity(profileId) })
    }

    override suspend fun updateStream(stream: StreamModel) {
        streamDao.updateStream(stream.toEntity("")) // Profile ID needed
    }

    override suspend fun deleteStream(streamId: String) {
        streamDao.deleteStreamById(streamId)
    }

    override suspend fun deleteAllStreamsForProfile(profileId: String) {
        streamDao.deleteAllStreamsForProfile(profileId)
    }
}
