package com.xtremeiptv.core.domain.usecase.profile

import com.xtremeiptv.core.domain.repository.ProfileRepository
import javax.inject.Inject

class SwitchProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: String): Result<Unit> =
        profileRepository.setActiveProfile(profileId)
}
