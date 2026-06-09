package fr.twentynine.keepon.domain.gateway

/**
 * Domain port that schedules applying [newScreenTimeout] to the system off the caller's thread
 * (the implementation enqueues background work). Set [updatePreviousTimeout] to record the outgoing
 * value as the "previous" timeout, so a later "go back to previous" resolves to it.
 */
interface ScreenTimeoutScheduler {
    fun schedule(newScreenTimeout: Int, updatePreviousTimeout: Boolean = false)
}
