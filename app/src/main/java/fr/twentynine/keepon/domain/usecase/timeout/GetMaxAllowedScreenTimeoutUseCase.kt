package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import javax.inject.Inject

/**
 * Returns the maximum screen timeout (ms) allowed by the active device policy.
 * [Long.MAX_VALUE] when no policy restricts it.
 */
class GetMaxAllowedScreenTimeoutUseCase @Inject constructor(
    private val devicePolicyController: DevicePolicyController,
) {
    operator fun invoke(): Long = devicePolicyController.getMaxAllowedScreenTimeout()
}
