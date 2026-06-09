package fr.twentynine.keepon.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Hourly safety net that re-arms the content-triggered [MonitorSystemScreenTimeoutWork]. The monitor
 * is one-shot and can be dropped by the system (process death, trigger loss); this periodic worker
 * re-schedules it so timeout monitoring keeps working even if the monitor was lost.
 */
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
