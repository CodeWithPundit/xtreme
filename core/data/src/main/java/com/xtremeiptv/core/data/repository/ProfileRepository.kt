package com.xtremeiptv.core.data.repository

import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.domain.model.ProtocolConfig
import com.xtremeiptv.core.domain.model.ProtocolType
import com.xtremeiptv.core.domain.repository.IProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val securePrefs: SecurePreferences
) : IProfileRepository {
    
    private val _activeProfile = MutableStateFlow<Profile?>(null)
    
    override fun getProfiles(): Flow<List<Profile>> =
        profileDao.getAllProfiles()
            .map { entities -> entities.map { it.toDomain() } }
    
    override fun getProfile(profileId: String): Flow<Profile?> =
        profileDao.getProfile(profileId)
            .map { it?.toDomain() }
    
    override fun getActiveProfile(): Flow<Profile?> = _activeProfile
    
    override suspend fun createProfile(
        name: String,
        protocolType: ProtocolType,
        protocolConfig: ProtocolConfig,
        avatarUrl: String?,
        pin: String?
    ): Result<Profile> = runCatching {
        val profile = Profile(
            id = generateProfileId(),
            name = name,
            avatarUrl = avatarUrl,
            pin = pin?.let { hashPin(it) },
            protocolType = protocolType,
            protocolConfig = encryptSensitiveData(protocolConfig),
            createdAt = System.currentTimeMillis()
        )
        
        profileDao.insertProfile(profile.toEntity())
        profile
    }
    
    override suspend fun updateProfile(profile: Profile): Result<Unit> = runCatching {
        profileDao.updateProfile(profile.toEntity())
    }
    
    override suspend fun deleteProfile(profileId: String): Result<Unit> = runCatching {
        profileDao.deleteProfile(profileId)
        if (_activeProfile.value?.id == profileId) {
            _activeProfile.value = null
        }
    }
    
    override suspend fun setActiveProfile(profileId: String): Result<Unit> = runCatching {
        val profile = profileDao.getProfileSync(profileId)?.toDomain()
            ?: throw IllegalArgumentException("Profile not found")
        _activeProfile.value = profile
        
        // Update last used timestamp
        profileDao.updateLastUsed(profileId, System.currentTimeMillis())
    }
    
    override suspend fun validatePin(profileId: String, pin: String): Boolean {
        val profile = profileDao.getProfileSync(profileId)?.toDomain() ?: return false
        return profile.pin?.let { verifyPin(pin, it) } ?: true
    }
    
    private fun generateProfileId(): String = "profile_${System.currentTimeMillis()}"
    
    private fun hashPin(pin: String): String {
        // Use secure hashing (bcrypt/scrypt) in production
        return pin // Placeholder
    }
    
    private fun verifyPin(input: String, hash: String): Boolean {
        return input == hash // Placeholder
    }
    
    private fun encryptSensitiveData(config: ProtocolConfig): ProtocolConfig {
        return when (config) {
            is ProtocolConfig.XtreamCodes -> config.copy(
                password = securePrefs.encrypt(config.password)
            )
            is ProtocolConfig.StalkerPortal -> config.copy(
                password = config.password?.let { securePrefs.encrypt(it) },
                token = config.token?.let { securePrefs.encrypt(it) }
            )
            else -> config
        }
    }
}
