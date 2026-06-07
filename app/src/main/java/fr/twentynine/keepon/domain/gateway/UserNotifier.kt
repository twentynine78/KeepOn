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
}
