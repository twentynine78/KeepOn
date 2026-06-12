package fr.twentynine.keepon.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.usecase.timeout.SynchronizeSystemTimeoutUseCase

/**
 * The core "watch the system timeout" worker. It is triggered by a content-URI observer on the
 * `SCREEN_OFF_TIMEOUT` setting (see [MonitorSystemScreenTimeoutWorkScheduler]), so it wakes whenever
 * the value changes; [SynchronizeSystemTimeoutUseCase] then reconciles the app's state with the new
 * value. It re-schedules itself each run, since a content-trigger worker is one-shot.
 */
@HiltWorker
class MonitorSystemScreenTimeoutWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val synchronizeSystemTimeoutUseCase: SynchronizeSystemTimeoutUseCase,
    private val tracer: DebugTracer,
) : CoroutineWorker(appContext, workerParams) {

    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun doWork(): Result {
        return try {
            tracer.trace(TAG) { "SCREEN_OFF_TIMEOUT change detected, synchronizing" }
            synchronizeSystemTimeoutUseCase()

            // Re-schedule the worker
            MonitorSystemScreenTimeoutWorkScheduler.scheduleWork(
                workManager = workManager,
                requeueIfRunning = true
            )
            tracer.trace(TAG) { "synchronized + monitor re-armed" }

            Result.success()
        } catch (e: Exception) {
            tracer.trace(TAG) { "synchronization failed: $e" }
            Result.failure()
        }
    }

    private companion object {
        const val TAG = "Monitor"
    }
}
