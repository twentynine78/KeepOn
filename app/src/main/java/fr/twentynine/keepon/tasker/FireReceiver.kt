package fr.twentynine.keepon.tasker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.tasker.TaskerIntent.Companion.ACTION_FIRE_SETTING
import fr.twentynine.keepon.tasker.TaskerIntent.Companion.EXTRA_BUNDLE
import fr.twentynine.keepon.tasker.PluginBundleManager.Companion.isBundleValid
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class FireReceiver : BroadcastReceiver() {

    private val bundleScrubber: BundleScrubber by lazy()
    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (bundleScrubber.scrub(intent)) {
            return
        }

        // Check that the Intent action will be ACTION_FIRE_SETTING
        if (ACTION_FIRE_SETTING != intent.action) {
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        val bundle = intent.getBundleExtra(EXTRA_BUNDLE)

        if (bundleScrubber.scrub(intent) || null == bundle || !isBundleValid(bundle)) {
            return
        }

        val timeoutValue = bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE)

        if ((timeoutValue == -42 || timeoutValue == -43 || preferences.getTimeoutValueArray().contains(timeoutValue)) && preferences.getAppIsLaunched()) {
            val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
            broadcastIntent.action = ServicesManagerReceiver.ACTION_SET_TIMEOUT
            resultCode = TaskerIntent.RESULT_CONDITION_SATISFIED
            broadcastIntent.putExtra("timeout", timeoutValue)
            context.sendBroadcast(broadcastIntent)

            resultCode = TaskerIntent.RESULT_CONDITION_SATISFIED
        } else {
            Toast.makeText(context.applicationContext, context.getString(R.string.initialization_not_ready), Toast.LENGTH_LONG).show()
            resultCode = TaskerIntent.RESULT_CONDITION_UNSATISFIED
        }
    }
}
