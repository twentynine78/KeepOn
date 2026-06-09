package fr.twentynine.keepon.core.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.domain.usecase.timeout.RestoreStateOnPackageReplacedUseCase
import fr.twentynine.keepon.domain.usecase.timeout.ScheduleDefaultTimeoutOnBootUseCase
import fr.twentynine.keepon.core.util.goAsync
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Restores KeepOn after a reboot or an app update: on `BOOT_COMPLETED` it re-applies the default
 * timeout, and on `MY_PACKAGE_REPLACED` it restores the saved state. Each handler runs asynchronously
 * via [goAsync]; implicit/foreign intents are ignored.
 */
@AndroidEntryPoint
class RebootAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleDefaultTimeoutOnBootUseCase: ScheduleDefaultTimeoutOnBootUseCase

    @Inject
    lateinit var restoreStateOnPackageReplacedUseCase: RestoreStateOnPackageReplacedUseCase

    override fun onReceive(context: Context, intent: Intent) {
        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                goAsync(Dispatchers.Default) {
                    scheduleDefaultTimeoutOnBootUseCase()
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                goAsync(Dispatchers.Default) {
                    restoreStateOnPackageReplacedUseCase()
                }
            }
        }
    }
}
