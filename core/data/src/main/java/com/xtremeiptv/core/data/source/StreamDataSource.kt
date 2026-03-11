package com.xtremeiptv.core.data.source

import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.model.StreamType
import kotlinx.coroutines.flow.Flow

interface StreamDataSource {
    fun getStreams(profileId: String, type: StreamType?): Flow<List<StreamModel>>
    fun getStream(streamId: String): Flow<StreamModel?>
    fun searchStreams(profileId: String, query: String, type: StreamType?): Flow<List<StreamModel>>
    suspend fun insertStreams(streams: List<StreamModel>, profileId: String)
    suspend fun updateStream(stream: StreamModel)
    suspend fun deleteStream(streamId: String)
    suspend fun deleteAllStreamsForProfile(profileId: String)
}
