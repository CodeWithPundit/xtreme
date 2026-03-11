package com.xtremeiptv.core.domain.usecase

import com.xtremeiptv.core.domain.model.StreamModel
import com.xtremeiptv.core.domain.repository.StreamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveStreamsUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    operator fun invoke(profileId: String): Flow<List<StreamModel>> =
        streamRepository.getLiveStreams(profileId)
}
