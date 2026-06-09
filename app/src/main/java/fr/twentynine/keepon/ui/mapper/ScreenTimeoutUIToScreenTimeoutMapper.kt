package fr.twentynine.keepon.ui.mapper

import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI

/** Strips a presentation [ScreenTimeoutUI] back to the domain [ScreenTimeout] (its raw value). */
object ScreenTimeoutUIToScreenTimeoutMapper : Mapper<ScreenTimeoutUI, ScreenTimeout> {
    override fun map(from: ScreenTimeoutUI): ScreenTimeout {
        return ScreenTimeout(from.value)
    }
}
