package fr.twentynine.keepon.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.extensions.goAsync
import fr.twentynine.keepon.worker.SetNewScreenTimeoutWorkScheduler
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class RebootAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var qsTileUpdater: QSTileUpdater

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    override fun onReceive(context: Context, intent: Intent) {
        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        val action = intent.action
        if (action == null) {
            return
        }

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                goAsync(Dispatchers.Default) {
                    // Reset the timeout to default at boot
                    if (userPreferencesRepository.getResetTimeoutWhenScreenOff()) {
                        val currentScreenTimeout = userPreferencesRepository.getCurrentScreenTimeout()
                        val defaultScreenTimeout = userPreferencesRepository.getDefaultScreenTimeout()

                        if (currentScreenTimeout != defaultScreenTimeout) {
                            SetNewScreenTimeoutWorkScheduler().scheduleWork(
                                SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value,
                                context.applicationContext
                            )
                        }
                    }
                    qsTileUpdater.requestUpdate()
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                goAsync(Dispatchers.Default) {
                    val keepOnIsActive = userPreferencesRepository.getKeepOnIsActive()
                    val resetWhenScreenOff = userPreferencesRepository.getResetTimeoutWhenScreenOff()

                    if (keepOnIsActive && resetWhenScreenOff) {
                        screenOffReceiverServiceManager.startService()
                    }
                    qsTileUpdater.requestUpdate()
                }
            }
        }
    }
}
