package fr.twentynine.keepon.util.coil

import coil3.key.Keyer
import coil3.request.Options
import fr.twentynine.keepon.data.model.TimeoutIconData

class TimeoutIconDataKeyer : Keyer<TimeoutIconData> {
    override fun key(data: TimeoutIconData, options: Options): String {
        return data.toString()
    }
}
