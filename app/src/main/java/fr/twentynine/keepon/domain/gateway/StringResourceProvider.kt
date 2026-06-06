package fr.twentynine.keepon.domain.gateway

/**
 * Domain port for resolving Android string/plural resources.
 *
 * Defined in the domain layer so business types (e.g. ScreenTimeout) can format
 * their display text without depending on the Android framework. The concrete
 * implementation lives in an outer layer and is bound via DI.
 */
interface StringResourceProvider {
    fun getString(resourceId: Int): String
    fun getPlural(resourceId: Int, count: Int): String
}
