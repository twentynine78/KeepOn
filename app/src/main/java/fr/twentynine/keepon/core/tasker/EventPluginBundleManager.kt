package fr.twentynine.keepon.core.tasker

import android.os.Bundle

/**
 * Defines the Tasker/Locale event plug-in bundle for KeepOn: the event-type extra key and a
 * validity check the plug-in's query receiver/edit activity uses to reject malformed bundles —
 * including the action plug-in's bundles, which carry a different key. Non-instantiable.
 */
class EventPluginBundleManager private constructor() {
    companion object {
        // Persisted in users' saved Tasker profiles — the literal must never change.
        internal const val BUNDLE_EXTRA_EVENT_TYPE = "fr.twentynine.keepon.tasker.EVENT_TYPE"

        /** True when [bundle] carries the expected event-type extra. */
        fun isBundleValid(bundle: Bundle?): Boolean {
            if (null == bundle) {
                return false
            }
            // Make sure the expected extras exist
            return bundle.containsKey(BUNDLE_EXTRA_EVENT_TYPE)
        }
    }

    init {
        throw UnsupportedOperationException("This class is non-instantiable")
    }
}
