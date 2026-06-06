package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.usecase.timeout.GetMaxAllowedScreenTimeoutUseCase
import fr.twentynine.keepon.ui.mapper.ScreenTimeoutToScreenTimeoutUIMapper
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import javax.inject.Inject

/**
 * Builds the presentation list of [ScreenTimeoutUI] from a list of domain timeouts,
 * computing each row's flags against the current device-policy maximum. Shared by the
 * main and Tasker-edit presentation paths so the flag + mapping logic lives in one place.
 *
 * A timeout is locked when it exceeds the device-policy maximum; selected when it is in
 * [selectedTimeouts] (and not locked) or is the default. [defaultTimeout] and
 * [currentTimeout] are nullable so callers that don't track them (the Tasker lists) get
 * those flags as false.
 */
class BuildScreenTimeoutUiListProducer @Inject constructor(
    private val stringResourceProvider: StringResourceProvider,
    private val getMaxAllowedScreenTimeoutUseCase: GetMaxAllowedScreenTimeoutUseCase,
) {
    operator fun invoke(
        timeouts: List<ScreenTimeout>,
        selectedTimeouts: List<ScreenTimeout> = emptyList(),
        defaultTimeout: ScreenTimeout? = null,
        currentTimeout: ScreenTimeout? = null,
    ): List<ScreenTimeoutUI> {
        val maxAllowedScreenTimeout = getMaxAllowedScreenTimeoutUseCase()

        return timeouts.map { screenTimeout ->
            val isLocked = screenTimeout.value > maxAllowedScreenTimeout
            val isDefault = screenTimeout == defaultTimeout
            val isCurrent = screenTimeout == currentTimeout
            val isSelected = (selectedTimeouts.contains(screenTimeout) && !isLocked) || isDefault

            ScreenTimeoutToScreenTimeoutUIMapper.map(
                screenTimeout = screenTimeout,
                stringResourceProvider = stringResourceProvider,
                isSelected = isSelected,
                isDefault = isDefault,
                isCurrent = isCurrent,
                isLocked = isLocked,
            )
        }
    }
}
