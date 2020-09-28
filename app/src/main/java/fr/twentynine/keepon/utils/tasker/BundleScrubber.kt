package fr.twentynine.keepon.utils.tasker

import android.content.Intent
import android.os.Bundle


object BundleScrubber {

    fun scrub(intent: Intent?): Boolean {
        return if (null == intent) {
            false
        } else scrub(intent.extras)
    }

    fun scrub(bundle: Bundle?): Boolean {
        if (null == bundle) {
            return false
        }

        // This is a hack to work around a private serializable classloader attack
        try {
            // if a private serializable exists, this will throw an exception
            bundle.containsKey(null)
        } catch (e: Exception) {
            bundle.clear()
            return true
        }
        return false
    }
}