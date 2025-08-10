package fr.twentynine.keepon.data.mapper

import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI

object ScreenTimeoutUIToScreenTimeoutMapper : Mapper<ScreenTimeoutUI, ScreenTimeout> {
    override fun map(from: ScreenTimeoutUI): ScreenTimeout {
        return ScreenTimeout(from.value)
    }
}
