package com.xtremeiptv.core.data.parser

import com.xtremeiptv.core.domain.model.EpgProgram
import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.domain.model.StreamModel

interface ProtocolParser {
    suspend fun parseLiveStreams(profile: Profile): Result<List<StreamModel>>
    suspend fun parseVOD(profile: Profile): Result<List<StreamModel>>
    suspend fun parseSeries(profile: Profile): Result<List<StreamModel>>
    suspend fun parseEPG(profile: Profile): Result<List<EpgProgram>>
    suspend fun authenticate(profile: Profile): Result<Boolean>
    suspend fun validateCredentials(profile: Profile): Result<Boolean>
    fun getCatchupUrl(stream: StreamModel, programStartTime: Long): String?
}
