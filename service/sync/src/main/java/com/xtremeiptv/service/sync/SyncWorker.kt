package com.xtremeiptv.service.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.xtremeiptv.core.data.repository.ProfileRepository
import com.xtremeiptv.core.data.repository.StreamRepository
import com.xtremeiptv.core.domain.model.ProtocolType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val activeProfile = profileRepository.getActiveProfile().first()
            
            if (activeProfile == null) {
                return@withContext Result.success()
            }

            // Refresh streams based on protocol type
            when (activeProfile.protocolType) {
                ProtocolType.M3U,
                ProtocolType.XTREAM_CODES -> {
                    streamRepository.refreshLiveStreams(activeProfile.id)
                    streamRepository.refreshEPG(activeProfile.id)
                }
                ProtocolType.STALKER_PORTAL,
                ProtocolType.MAC_PORTAL -> {
                    streamRepository.refreshLiveStreams(activeProfile.id)
                    streamRepository.refreshEPG(activeProfile.id)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "sync_worker"
        
        fun startPeriodicSync() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                6, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun stopPeriodicSync() {
            WorkManager.getInstance().cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        fun startOneTimeSync() {
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        }
    }
}
