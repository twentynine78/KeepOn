package fr.twentynine.keepon.domain.gateway

interface ScreenTimeoutScheduler {
    fun schedule(newScreenTimeout: Int, updatePreviousTimeout: Boolean = false)
}
