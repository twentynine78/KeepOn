package fr.twentynine.keepon.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.time.Duration

class SetNewScreenTimeoutWorkScheduler {

    private val workRequestConstraints = Constraints.Builder().build()

    fun scheduleWork(newScreenTimeout: Int, context: Context, updatePreviousTimeout: Boolean = false) {
        val dataBuilder = Data.Builder()
        dataBuilder.putInt(NEW_SCREEN_TIMEOUT_DATA_KEY, newScreenTimeout)
        dataBuilder.putBoolean(UPDATE_PREVIOUS_TIMEOUT_DATA_KEY, updatePreviousTimeout)

        val setNewScreenTimeoutWorkRequest = OneTimeWorkRequestBuilder<SetNewScreenTimeoutWork>()
            .setConstraints(workRequestConstraints)
            .setInitialDelay(Duration.ZERO)
            .setBackoffCriteria(BackoffPolicy.LINEAR, Duration.ofMillis(WorkRequest.MIN_BACKOFF_MILLIS))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(dataBuilder.build())
            .build()

        val workManager: WorkManager = WorkManager.getInstance(context.applicationContext)

        workManager.enqueueUniqueWork(
            NEW_SCREEN_TIMEOUT_WORKER,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            setNewScreenTimeoutWorkRequest
        )
    }

    companion object {
        private const val NEW_SCREEN_TIMEOUT_WORKER = "new_screen_timeout_worker"
        const val NEW_SCREEN_TIMEOUT_DATA_KEY = "new_screen_timeout_data"
        const val UPDATE_PREVIOUS_TIMEOUT_DATA_KEY = "update_previous_timeout_data"
    }
}
