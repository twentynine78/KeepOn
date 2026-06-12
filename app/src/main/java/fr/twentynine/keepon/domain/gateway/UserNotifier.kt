package fr.twentynine.keepon.domain.gateway

/**
 * Domain port for surfacing brief, transient feedback to the user (implemented as a
 * toast in the outer layer). Methods are semantic so the domain stays free of any
 * Android resource identifier.
 */
interface UserNotifier {
    /**
     * Notifies the user that the screen-timeout change was not applied by the system
     * (some OEM ROMs silently ignore the write: the call succeeds but the value never
     * changes).
     */
    fun notifyScreenTimeoutNotApplied()

    /**
     * Notifies the user that the system battery-optimization request screen could not
     * be opened (some OEM ROMs ship without a handler for it).
     */
    fun notifyBatteryOptimizationRequestUnavailable()

    /**
     * Notifies the user that an action was skipped because a required permission
     * (write-settings or battery-optimization exemption) is missing or was revoked.
     */
    fun notifyMissingPermission()

    /** Notifies the user that the screen-off monitoring service failed to start or stop. */
    fun notifyScreenOffServiceError()

    /**
     * Notifies the user that a Tasker action fired with a timeout value the app doesn't
     * know, so the change was skipped.
     */
    fun notifyInvalidScreenTimeout()
}
