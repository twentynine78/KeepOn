package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.ScreenTimeout

/**
 * Domain port exposing device-policy constraints on the screen timeout
 * (maximum allowed value enforced by a device admin / MDM).
 */
interface DevicePolicyController {
    fun getMaxAllowedScreenTimeout(): Long
    fun isValidTimeout(timeout: ScreenTimeout): Boolean
    fun removeNotAllowedScreenTimeout(timeouts: List<ScreenTimeout>): List<ScreenTimeout>
}
