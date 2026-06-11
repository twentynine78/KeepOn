package fr.twentynine.keepon.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-lifetime gate for the first-launch swipe hint: the hint plays at most once per cold app
 * start — not again on activity recreation, nor when re-enabling the reset option re-composes the
 * swipe rows while the persisted isFirstLaunch flag is still true (it stays true over the first few
 * launches, until the swipe gesture is actually used).
 */
@Singleton
class FirstLaunchHintGate @Inject constructor() {
    private val _hintPlayed = MutableStateFlow(false)
    val hintPlayed: StateFlow<Boolean> = _hintPlayed.asStateFlow()

    fun markHintPlayed() {
        _hintPlayed.value = true
    }
}
