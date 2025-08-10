package fr.twentynine.keepon.worker

import android.content.Context
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.RequiredPermissionsManager
import fr.twentynine.keepon.worker.SetNewScreenTimeoutWorkScheduler.Companion.NEW_SCREEN_TIMEOUT_DATA_KEY
import fr.twentynine.keepon.worker.SetNewScreenTimeoutWorkScheduler.Companion.UPDATE_PREVIOUS_TIMEOUT_DATA_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SetNewScreenTimeoutWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val qsTileUpdater: QSTileUpdater,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (RequiredPermissionsManager.isPermissionsGranted(appContext)) {
                val newScreenTimeout = inputData.getInt(NEW_SCREEN_TIMEOUT_DATA_KEY, -1)
                val updatePreviousTimeout = inputData.getBoolean(UPDATE_PREVIOUS_TIMEOUT_DATA_KEY, false)

                if (newScreenTimeout != -1) {
                    userPreferencesRepository.setNewSystemScreenTimeout(
                        ScreenTimeout(newScreenTimeout),
                        updatePreviousTimeout
                    ) {
                        qsTileUpdater.requestUpdate()
                    }
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
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
