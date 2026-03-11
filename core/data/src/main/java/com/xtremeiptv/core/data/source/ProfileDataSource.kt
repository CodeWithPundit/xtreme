package com.xtremeiptv.core.data.source

import com.xtremeiptv.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileDataSource {
    fun getProfiles(): Flow<List<Profile>>
    fun getProfile(profileId: String): Flow<Profile?>
    fun getActiveProfile(): Flow<Profile?>
    suspend fun insertProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(profileId: String)
    suspend fun setActiveProfile(profileId: String)
    suspend fun deactivateAllProfiles()
}
