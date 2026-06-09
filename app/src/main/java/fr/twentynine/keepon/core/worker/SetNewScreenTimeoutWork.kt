package fr.twentynine.keepon.core.worker

import android.content.Context
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.usecase.timeout.UpdateSystemScreenTimeoutUseCase
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler.Companion.NEW_SCREEN_TIMEOUT_DATA_KEY
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler.Companion.UPDATE_PREVIOUS_TIMEOUT_DATA_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Applies a requested timeout off the caller's thread via [UpdateSystemScreenTimeoutUseCase], reading
 * the target value (and the "record previous" flag) from its input data. Toasts and stops when the
 * required permissions are missing or a write is denied; retries on other transient errors.
 */
@HiltWorker
class SetNewScreenTimeoutWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateSystemScreenTimeoutUseCase: UpdateSystemScreenTimeoutUseCase,
    private val permissionStateGateway: PermissionStateGateway,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (permissionStateGateway.areRequiredPermissionsGranted()) {
                val newScreenTimeout = inputData.getInt(NEW_SCREEN_TIMEOUT_DATA_KEY, -1)
                val updatePreviousTimeout = inputData.getBoolean(UPDATE_PREVIOUS_TIMEOUT_DATA_KEY, false)

                if (newScreenTimeout != -1) {
                    updateSystemScreenTimeoutUseCase(
                        ScreenTimeout(newScreenTimeout),
                        updatePreviousTimeout
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.toast_missing_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            Result.success()
        } catch (_: SecurityException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    appContext,
                    appContext.getString(R.string.toast_missing_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
            Result.failure()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
