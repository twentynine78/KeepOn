package fr.twentynine.keepon.core.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.core.util.goAsync
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class RebootAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var appComponentsUpdater: AppComponentsUpdater

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    override fun onReceive(context: Context, intent: Intent) {
        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        val action = intent.action ?: return

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
                    appComponentsUpdater.requestUpdate()
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                goAsync(Dispatchers.Default) {
                    val keepOnIsActive = userPreferencesRepository.getKeepOnIsActive()
                    val resetWhenScreenOff = userPreferencesRepository.getResetTimeoutWhenScreenOff()

                    if (keepOnIsActive && resetWhenScreenOff) {
                        screenOffReceiverServiceManager.startService()
                    }
                    appComponentsUpdater.requestUpdate()
                }
            }
        }
    }
}
