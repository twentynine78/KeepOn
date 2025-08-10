package fr.twentynine.keepon.util

import android.content.Intent
import android.os.Bundle

object BundleScrubber {
    fun scrub(intent: Intent?): Boolean {
        return if (null == intent) {
            false
        } else {
            scrub(intent.extras)
        }
    }

    private fun scrub(bundle: Bundle?): Boolean {
        if (null == bundle) {
            return false
        }

        // This is a hack to work around a private serializable classloader attack
        try {
            // if a private serializable exists, this will throw an exception
            bundle.containsKey(null)
        } catch (_: Exception) {
            bundle.clear()
            return true
        }
        return false
    }
}
