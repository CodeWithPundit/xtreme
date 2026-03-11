package com.xtremeiptv.core.database.di

import android.content.Context
import androidx.room.Room
import com.xtremeiptv.core.database.AppDatabase
import com.xtremeiptv.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "xtreme_iptv_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideStreamDao(database: AppDatabase): StreamDao {
        return database.streamDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideEpgProgramDao(database: AppDatabase): EpgProgramDao {
        return database.epgProgramDao()
    }

    @Provides
    @Singleton
    fun provideEpgChannelDao(database: AppDatabase): EpgChannelDao {
        return database.epgChannelDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideContinueWatchingDao(database: AppDatabase): ContinueWatchingDao {
        return database.continueWatchingDao()
    }
}
