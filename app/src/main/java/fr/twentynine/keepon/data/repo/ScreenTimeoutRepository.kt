package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.model.ScreenTimeout
import kotlinx.collections.immutable.toPersistentList

object ScreenTimeoutRepository {
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
