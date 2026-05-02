package fr.twentynine.keepon.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class GuardianSystemScreenTimeoutWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val workManager = WorkManager.getInstance(appContext)

        // Ensure the monitor worker with ContentUriTrigger is correctly scheduled
        MonitorSystemScreenTimeoutWorkScheduler.scheduleWork(workManager)

        return Result.success()
    }
}
