package fr.twentynine.keepon.domain.catalog

import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.model.ScreenTimeout
import kotlinx.collections.immutable.toPersistentList

/**
 * The fixed set of timeouts the app offers. [screenTimeouts] are the real, selectable durations
 * (15s → 1h, plus never-sleep); [specialScreenTimeouts] are the two sentinel actions
 * ([SpecialScreenTimeoutType]) used by Tasker and the cycle logic. Both are immutable lists.
 */
object ScreenTimeoutCatalog {
    val screenTimeouts = listOf(
        ScreenTimeout(
            value = 15000
        ),
        ScreenTimeout(
            value = 30000
        ),
        ScreenTimeout(
            value = 60000
        ),
        ScreenTimeout(
            value = 120000
        ),
        ScreenTimeout(
            value = 300000
        ),
        ScreenTimeout(
            value = 600000
        ),
        ScreenTimeout(
            value = 1800000
        ),
        ScreenTimeout(
            value = 3600000
        ),
        ScreenTimeout(
            value = Int.MAX_VALUE
        )
    ).toPersistentList()

    val specialScreenTimeouts = listOf(
        ScreenTimeout(
            value = SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value
        ),
        ScreenTimeout(
            value = SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value
        )
    ).toPersistentList()
}
