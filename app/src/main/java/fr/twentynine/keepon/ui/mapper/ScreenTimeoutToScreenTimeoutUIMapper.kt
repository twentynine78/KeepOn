package fr.twentynine.keepon.ui.mapper

import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI

/**
 * Stateless mapper from a domain [ScreenTimeout] to its presentation [ScreenTimeoutUI].
 * All presentation flags are passed in as arguments (defaulting to false), so the
 * mapper holds no shared mutable state and is safe to call concurrently.
 */
object ScreenTimeoutToScreenTimeoutUIMapper {
    fun map(
        screenTimeout: ScreenTimeout,
        stringResourceProvider: StringResourceProvider,
        isSelected: Boolean = false,
        isDefault: Boolean = false,
        isCurrent: Boolean = false,
        isLocked: Boolean = false,
    ): ScreenTimeoutUI {
        return ScreenTimeoutUI(
            value = screenTimeout.value,
            displayName = screenTimeout.getFullDisplayTimeout(stringResourceProvider),
            isSelected = isSelected,
            isDefault = isDefault,
            isCurrent = isCurrent,
            isLocked = isLocked,
        )
    }
}
