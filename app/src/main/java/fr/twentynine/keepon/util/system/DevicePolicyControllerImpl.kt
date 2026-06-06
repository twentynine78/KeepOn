package fr.twentynine.keepon.util.system

import android.app.admin.DevicePolicyManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import javax.inject.Inject

class DevicePolicyControllerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : DevicePolicyController {

    private val devicePolicyManager by lazy {
        context.getSystemService(DevicePolicyManager::class.java)
    }

    override fun getMaxAllowedScreenTimeout(): Long {
        var maxAllowedTimeout = devicePolicyManager.getMaximumTimeToLock(null)
        if (maxAllowedTimeout == 0L) maxAllowedTimeout = Long.MAX_VALUE

        return maxAllowedTimeout
    }

    override fun isValidTimeout(timeout: ScreenTimeout): Boolean {
        return timeout.value <= getMaxAllowedScreenTimeout() && timeout.value != -1
    }

    override fun removeNotAllowedScreenTimeout(timeouts: List<ScreenTimeout>): List<ScreenTimeout> {
        val maxAllowedScreenTimeout = getMaxAllowedScreenTimeout()
        return timeouts.filter { timeout ->
            timeout.value <= maxAllowedScreenTimeout
        }
    }
}
