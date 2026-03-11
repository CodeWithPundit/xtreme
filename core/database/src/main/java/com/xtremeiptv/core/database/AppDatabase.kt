package com.xtremeiptv.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.xtremeiptv.core.database.dao.*
import com.xtremeiptv.core.database.entity.*
import com.xtremeiptv.core.database.converter.Converters

@Database(
    entities = [
        ProfileEntity::class,
        StreamEntity::class,
        CategoryEntity::class,
        EpgProgramEntity::class,
        EpgChannelEntity::class,
        DownloadEntity::class,
        HistoryEntity::class,
        FavoriteEntity::class,
        ContinueWatchingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun streamDao(): StreamDao
    abstract fun categoryDao(): CategoryDao
    abstract fun epgProgramDao(): EpgProgramDao
    abstract fun epgChannelDao(): EpgChannelDao
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun continueWatchingDao(): ContinueWatchingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xtreme_iptv_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
