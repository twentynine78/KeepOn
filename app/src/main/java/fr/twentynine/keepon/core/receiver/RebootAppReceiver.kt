package fr.twentynine.keepon.core.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.ScreenTimeoutScheduler
import fr.twentynine.keepon.core.util.goAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class RebootAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var timeoutPreferencesRepository: TimeoutPreferencesRepository

    @Inject
    lateinit var getKeepOnStatusUseCase: GetKeepOnStatusUseCase

    @Inject
    lateinit var appComponentsUpdater: AppComponentsUpdater

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    @Inject
    lateinit var screenTimeoutScheduler: ScreenTimeoutScheduler

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
                    if (timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()) {
                        val currentScreenTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
                        val defaultScreenTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()

                        if (currentScreenTimeout != defaultScreenTimeout) {
                            screenTimeoutScheduler.schedule(SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value)
                        }
                    }
                    appComponentsUpdater.requestUpdate()
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                goAsync(Dispatchers.Default) {
                    val keepOnIsActive = getKeepOnStatusUseCase().firstOrNull() ?: false
                    val resetWhenScreenOff = timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()

                    if (keepOnIsActive && resetWhenScreenOff) {
                        screenOffReceiverServiceManager.startService()
                    }
                    appComponentsUpdater.requestUpdate()
                }
            }
        }
    }
}
