package com.xtremeiptv.core.domain.usecase

import com.xtremeiptv.core.domain.model.EpgProgram
import com.xtremeiptv.core.domain.repository.StreamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEPGForChannelUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    operator fun invoke(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<EpgProgram>> =
        streamRepository.getEPGForChannel(channelId, startTime, endTime)
}
