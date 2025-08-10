package fr.twentynine.keepon.worker

import android.provider.Settings
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import fr.twentynine.keepon.util.extensions.uuid
import java.time.Duration

object MonitorSystemScreenTimeoutWorkScheduler {

    private const val SYSTEM_SCREEN_TIMEOUT_WORKER = "system_screen_timeout_worker"
    private val SYSTEM_SCREEN_TIMEOUT_WORKER_ID = SYSTEM_SCREEN_TIMEOUT_WORKER.uuid()

    private val screenTimeoutSettingContentUri = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)

    private val workRequestConstraints = Constraints.Builder()
        .addContentUriTrigger(screenTimeoutSettingContentUri, true)
        .setTriggerContentUpdateDelay(Duration.ZERO)
        .build()

    private val monitorSystemScreenTimeoutWorkRequest = OneTimeWorkRequestBuilder<MonitorSystemScreenTimeoutWork>()
        .setId(SYSTEM_SCREEN_TIMEOUT_WORKER_ID)
        .setConstraints(workRequestConstraints)
        .setInitialDelay(Duration.ZERO)
        .setBackoffCriteria(BackoffPolicy.LINEAR, Duration.ofMillis(WorkRequest.MIN_BACKOFF_MILLIS))
        .build()

    fun scheduleWork(workManager: WorkManager, requeueIfRunning: Boolean = false) {
        workManager.pruneWork()
        val workInfo = workManager.getWorkInfoById(SYSTEM_SCREEN_TIMEOUT_WORKER_ID).get()
        if (workInfo == null || requeueIfRunning) {
            workManager.enqueueUniqueWork(
                SYSTEM_SCREEN_TIMEOUT_WORKER,
                ExistingWorkPolicy.REPLACE,
                monitorSystemScreenTimeoutWorkRequest
            )
        }
    }
}
