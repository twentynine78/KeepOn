package fr.twentynine.keepon.core.util

import android.content.Intent
import android.os.Bundle

/**
 * Defuses the "private serializable" classloader attack on incoming intents: touching a bundle that
 * carries a serializable the app can't load throws, so this probes it and, on failure, clears the
 * extras. Call it on any intent received from outside the app before reading its extras.
 */
object BundleScrubber {
    /** Scrubs [intent]'s extras in place; returns true when a hostile bundle was detected and cleared. */
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
