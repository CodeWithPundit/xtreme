package com.xtremeiptv.core.data.source.local

import com.xtremeiptv.core.database.dao.ProfileDao
import com.xtremeiptv.core.database.entity.ProfileEntity
import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.data.source.ProfileDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalProfileDataSource @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileDataSource {

    override fun getProfiles(): Flow<List<Profile>> =
        profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getProfile(profileId: String): Flow<Profile?> =
        profileDao.getProfile(profileId).map { it?.toDomain() }

    override fun getActiveProfile(): Flow<Profile?> =
        profileDao.getActiveProfile().map { it?.toDomain() }

    override suspend fun insertProfile(profile: Profile) {
        profileDao.insertProfile(profile.toEntity())
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(profileId: String) {
        profileDao.deleteProfile(profileId)
    }

    override suspend fun setActiveProfile(profileId: String) {
        profileDao.deactivateAllProfiles()
        profileDao.setActiveProfile(profileId, System.currentTimeMillis())
    }

    override suspend fun deactivateAllProfiles() {
        profileDao.deactivateAllProfiles()
    }
}
