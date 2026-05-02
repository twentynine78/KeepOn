package fr.twentynine.keepon.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object GuardianSystemScreenTimeoutWorkerScheduler {

    private const val GUARDIAN_SYSTEM_SCREEN_TIMEOUT_WORKER = "guardian_system_screen_timeout_worker"

    fun scheduleGuardianWork(workManager: WorkManager) {
        val guardianRequest = PeriodicWorkRequestBuilder<GuardianSystemScreenTimeoutWorker>(
            4, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            GUARDIAN_SYSTEM_SCREEN_TIMEOUT_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            guardianRequest
        )
    }
}