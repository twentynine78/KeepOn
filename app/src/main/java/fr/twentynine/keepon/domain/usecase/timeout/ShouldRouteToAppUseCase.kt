package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import javax.inject.Inject

/**
 * Whether a QS tile / widget tap should open the app instead of cycling the screen timeout:
 * true when the timeout cannot be cycled (no selectable value other than the current one) or a
 * required permission is missing. Shared by the QS tile and the widget action callback so both
 * route to the app under exactly the same conditions, evaluated live at click time.
 */
class ShouldRouteToAppUseCase @Inject constructor(
    private val canCycleScreenTimeoutUseCase: CanCycleScreenTimeoutUseCase,
    private val permissionStateGateway: PermissionStateGateway,
) {
    suspend operator fun invoke(): Boolean =
        !canCycleScreenTimeoutUseCase() || !permissionStateGateway.areRequiredPermissionsGranted()
}
