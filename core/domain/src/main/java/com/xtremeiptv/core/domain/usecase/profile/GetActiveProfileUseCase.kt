package com.xtremeiptv.core.domain.usecase.profile

import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<Profile?> =
        profileRepository.getActiveProfile()
}
