package fr.twentynine.keepon.domain.gateway

/**
 * Domain port to clear the in-memory icon cache (Coil), so a new icon style
 * is regenerated instead of being served from cache.
 */
interface MemoryCacheManager {
    fun clear()
}
