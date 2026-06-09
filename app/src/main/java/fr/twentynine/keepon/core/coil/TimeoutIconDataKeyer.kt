package fr.twentynine.keepon.core.coil

import coil3.key.Keyer
import coil3.request.Options
import fr.twentynine.keepon.domain.model.TimeoutIconData

/**
 * Coil [Keyer] for [TimeoutIconData]: uses the data class's own `toString()` as the cache key, so two
 * requests with the same timeout/size/style hit the same generated bitmap in the memory cache.
 */
class TimeoutIconDataKeyer : Keyer<TimeoutIconData> {
    override fun key(data: TimeoutIconData, options: Options): String {
        return data.toString()
    }
}
