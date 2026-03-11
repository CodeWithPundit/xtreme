package com.xtremeiptv.core.database.dao

import androidx.room.*
import com.xtremeiptv.core.database.entity.StreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {

    @Query("SELECT * FROM streams WHERE profile_id = :profileId AND stream_type = :streamType")
    fun getStreamsByType(profileId: String, streamType: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE profile_id = :profileId AND category_id = :categoryId")
    fun getStreamsByCategory(profileId: String, categoryId: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE profile_id = :profileId AND group_name = :groupName")
    fun getStreamsByGroup(profileId: String, groupName: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE stream_id = :streamId")
    fun getStream(streamId: String): Flow<StreamEntity?>

    @Query("SELECT * FROM streams WHERE profile_id = :profileId AND title LIKE '%' || :query || '%'")
    fun searchStreams(profileId: String, query: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE profile_id = :profileId AND stream_type = :streamType AND title LIKE '%' || :query || '%'")
    fun searchStreamsByType(profileId: String, query: String, streamType: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE profile_id = :profileId ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentStreams(profileId: String, limit: Int = 20): Flow<List<StreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: StreamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<StreamEntity>)

    @Update
    suspend fun updateStream(stream: StreamEntity)

    @Delete
    suspend fun deleteStream(stream: StreamEntity)

    @Query("DELETE FROM streams WHERE profile_id = :profileId")
    suspend fun deleteAllStreamsForProfile(profileId: String)

    @Query("DELETE FROM streams")
    suspend fun deleteAllStreams()

    @Query("SELECT COUNT(*) FROM streams WHERE profile_id = :profileId AND stream_type = :streamType")
    suspend fun getStreamCount(profileId: String, streamType: String): Int
}
