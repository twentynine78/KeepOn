package fr.twentynine.keepon.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.usecase.timeout.UpdateSystemScreenTimeoutUseCase
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.gateway.UserNotifier
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler.Companion.NEW_SCREEN_TIMEOUT_DATA_KEY
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler.Companion.UPDATE_PREVIOUS_TIMEOUT_DATA_KEY

/**
 * Applies a requested timeout off the caller's thread via [UpdateSystemScreenTimeoutUseCase], reading
 * the target value (and the "record previous" flag) from its input data. Notifies and stops when the
 * required permissions are missing or a write is denied; retries on other transient errors.
 */
@HiltWorker
class SetNewScreenTimeoutWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateSystemScreenTimeoutUseCase: UpdateSystemScreenTimeoutUseCase,
    private val permissionStateGateway: PermissionStateGateway,
    private val userNotifier: UserNotifier,
    private val tracer: DebugTracer,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (permissionStateGateway.areRequiredPermissionsGranted()) {
                val newScreenTimeout = inputData.getInt(NEW_SCREEN_TIMEOUT_DATA_KEY, -1)
                val updatePreviousTimeout = inputData.getBoolean(UPDATE_PREVIOUS_TIMEOUT_DATA_KEY, false)

                if (newScreenTimeout != -1) {
                    tracer.trace(TAG) {
                        "applying $newScreenTimeout (updatePrevious=$updatePreviousTimeout)"
                    }
                    updateSystemScreenTimeoutUseCase(
                        ScreenTimeout(newScreenTimeout),
                        updatePreviousTimeout
                    )
                }
            } else {
                tracer.trace(TAG) { "skipped: required permissions missing" }
                userNotifier.notifyMissingPermission()
            }

            Result.success()
        } catch (_: SecurityException) {
            tracer.trace(TAG) { "SecurityException: WRITE_SETTINGS revoked mid-run" }
            userNotifier.notifyMissingPermission()
            Result.failure()
        } catch (e: Exception) {
            tracer.trace(TAG) { "transient failure, retrying: $e" }
            Result.retry()
        }
    }

    private companion object {
        const val TAG = "ApplyWork"
    }
}
