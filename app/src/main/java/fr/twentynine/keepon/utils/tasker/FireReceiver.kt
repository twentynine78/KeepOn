package fr.twentynine.keepon.utils.tasker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.tasker.PluginBundleManager.Companion.isBundleValid


class FireReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent))
            return

        // Check that the Intent action will be ACTION_FIRE_SETTING
        if (fr.twentynine.keepon.utils.tasker.Intent.ACTION_FIRE_SETTING != intent.action)
            return

        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component)
            return

        val bundle = intent.getBundleExtra(fr.twentynine.keepon.utils.tasker.Intent.EXTRA_BUNDLE)

        if (BundleScrubber.scrub(intent) || null == bundle || !isBundleValid(bundle))
            return

        val timeoutValue = bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE)

        val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
        broadcastIntent.action = ServicesManagerReceiver.ACTION_SET_TIMEOUT
        broadcastIntent.putExtra("timeout", timeoutValue)

        context.sendBroadcast(broadcastIntent)
    }
}