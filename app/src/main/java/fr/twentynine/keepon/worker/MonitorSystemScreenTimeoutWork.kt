package fr.twentynine.keepon.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.domain.usecase.timeout.SynchronizeSystemTimeoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MonitorSystemScreenTimeoutWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val synchronizeSystemTimeoutUseCase: SynchronizeSystemTimeoutUseCase,
) : CoroutineWorker(appContext, workerParams) {

    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                synchronizeSystemTimeoutUseCase()

                // Re-schedule the worker
                MonitorSystemScreenTimeoutWorkScheduler.scheduleWork(
                    workManager = workManager,
                    requeueIfRunning = true
                )

                Result.success()
            } catch (_: Exception) {
                Result.failure()
            }
        }
    }
}
