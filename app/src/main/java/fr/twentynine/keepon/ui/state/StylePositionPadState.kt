package fr.twentynine.keepon.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-lifetime holder for the Style screen's position-pad expansion: deliberately not
 * persisted, it only has to outlive the activity recreation of a configuration change so the pad
 * stays unfolded across a rotation. The screen explicitly folds it back when the pad leaves the
 * composition for any other reason (scroll-away, tab change, activity finish).
 */
@Singleton
class StylePositionPadState @Inject constructor() {
    private val _expanded = MutableStateFlow(false)
    val expanded: StateFlow<Boolean> = _expanded.asStateFlow()

    fun setExpanded(value: Boolean) {
        _expanded.value = value
    }
}
