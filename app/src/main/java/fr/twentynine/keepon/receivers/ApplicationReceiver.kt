package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.utils.CommonUtils
import toothpick.ktp.delegate.lazy

class ApplicationReceiver : BroadcastReceiver() {

    private val commonUtils: CommonUtils by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }
        val action = intent.action

        if (action != null) {
            when (action) {
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    commonUtils.startScreenTimeoutObserverService()
                }
                CommonUtils.ACTION_MANAGE_SHORTCUTS -> {
                    commonUtils.createShortcuts()
                }
            }
        }
    }
}
