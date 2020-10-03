package fr.twentynine.keepon.tasker

import android.os.Bundle

class PluginBundleManager private constructor() {
    companion object {
        const val BUNDLE_EXTRA_TIMEOUT_VALUE = "fr.twentynine.keepon.tasker.TIMEOUT_VALUE"

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
