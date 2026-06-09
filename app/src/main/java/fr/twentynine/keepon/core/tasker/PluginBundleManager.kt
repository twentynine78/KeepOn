package fr.twentynine.keepon.core.tasker

import android.os.Bundle

/**
 * Defines the Tasker/Locale plug-in bundle for KeepOn: the timeout-value extra key and a validity
 * check the plug-in's receiver/edit activity uses to reject malformed bundles. Non-instantiable.
 */
class PluginBundleManager private constructor() {
    companion object {
        internal const val BUNDLE_EXTRA_TIMEOUT_VALUE = "fr.twentynine.keepon.tasker.TIMEOUT_VALUE"

        /** True when [bundle] carries the expected timeout-value extra. */
        fun isBundleValid(bundle: Bundle?): Boolean {
            if (null == bundle) {
                return false
            }
            // Make sure the expected extras exist
            return bundle.containsKey(BUNDLE_EXTRA_TIMEOUT_VALUE)
        }
    }

    init {
        throw UnsupportedOperationException("This class is non-instantiable")
    }
}
