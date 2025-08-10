package fr.twentynine.keepon.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

class LockableJob {
    var job: Job? = null

    enum class LockableJobState {
        LOCKED, UNLOCKED
    }

    private var state: LockableJobState = LockableJobState.UNLOCKED

    fun lock() {
        state = LockableJobState.LOCKED
    }

    fun unlock() {
        state = LockableJobState.UNLOCKED
    }

    suspend fun cancelOrJoin() {
        when (state) {
            LockableJobState.LOCKED -> job?.join()
            LockableJobState.UNLOCKED -> job?.cancelAndJoin()
        }
        unlock()
    }
}
