package fr.twentynine.keepon.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.util.DesiredScreenTimeoutController
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.SystemScreenTimeoutController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MonitorSystemScreenTimeoutWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
    private val qsTileUpdater: QSTileUpdater,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
) : CoroutineWorker(appContext, workerParams) {

    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // Get desired screen timeout
                val currentScreenTimeout = systemScreenTimeoutController.getSystemScreenTimeout()
                val desiredScreenTimeout = DesiredScreenTimeoutController.getDesiredScreenTimeout(currentScreenTimeout)

                // Update current screen timeout with new data
                updateCurrentSystemScreenTimeout(currentScreenTimeout, desiredScreenTimeout)

                // Update the QS tile
                qsTileUpdater.requestUpdate()

                // Re-schedule the worker
                MonitorSystemScreenTimeoutWorkScheduler.scheduleWork(
                    workManager = workManager,
                    requeueIfRunning = true
                )

                Result.success()
            } catch (e: Exception) {
                Log.e("MonitorSystemScreenTimeoutWork", "Error monitoring system screen timeout", e)
                Result.failure()
            }
        }
    }

    private suspend fun updateCurrentSystemScreenTimeout(
        currentScreenTimeout: ScreenTimeout,
        desiredScreenTimeout: ScreenTimeout?
    ) {
        // Check if the new timeout is initiated by the app with the desiredScreenTimeout value
        if (desiredScreenTimeout == currentScreenTimeout) {
            val startScreenOffReceiverService = currentScreenTimeout != userPreferencesRepository.getDefaultScreenTimeout() &&
                userPreferencesRepository.getResetTimeoutWhenScreenOff()

            if (startScreenOffReceiverService) {
                screenOffReceiverServiceManager.startService()
            } else {
                screenOffReceiverServiceManager.stopService()
            }
        } else {
            userPreferencesRepository.setDefaultScreenTimeout(currentScreenTimeout)

            screenOffReceiverServiceManager.stopService()
        }

        userPreferencesRepository.setCurrentScreenTimeout(currentScreenTimeout)
    }
}
