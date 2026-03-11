package com.xtremeiptv.core.database.dao

import androidx.room.*
import com.xtremeiptv.core.database.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE profile_id = :profileId")
    fun getProfile(profileId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE profile_id = :profileId")
    suspend fun getProfileSync(profileId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE is_active = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE profile_id = :profileId")
    suspend fun deleteProfile(profileId: String)

    @Query("UPDATE profiles SET is_active = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE profiles SET is_active = 1, last_used = :lastUsed WHERE profile_id = :profileId")
    suspend fun setActiveProfile(profileId: String, lastUsed: Long)

    @Query("UPDATE profiles SET last_used = :lastUsed WHERE profile_id = :profileId")
    suspend fun updateLastUsed(profileId: String, lastUsed: Long)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}
