package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.ui.state.WidgetUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Produces the widget presentation state as a reactive [Flow]: the Glance widget
 * collects it in `provideContent` (via an `@EntryPoint`). Combines the domain flows
 * the widget needs and emits an [WidgetUIState.Error] if anything fails upstream.
 */
class WidgetStateProducer @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val getKeepOnStatusUseCase: GetKeepOnStatusUseCase,
) {
    suspend operator fun invoke(): Flow<WidgetUIState> =
        combine(
            timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
            getKeepOnStatusUseCase(),
            uiPreferencesRepository.getTimeoutIconStyleFlow(),
            timeoutPreferencesRepository.getSelectedScreenTimeoutFlow(),
            timeoutPreferencesRepository.getDefaultScreenTimeoutFlow(),
        ) { currentScreenTimeout, keepOnIsActive, timeoutIconStyle, selectedTimeouts, defaultTimeout ->
            WidgetUIState.Success(
                currentScreenTimeout = currentScreenTimeout,
                keepOnIsActive = keepOnIsActive,
                timeoutIconStyle = timeoutIconStyle,
                selectedTimeouts = selectedTimeouts,
                defaultTimeout = defaultTimeout,
            ) as WidgetUIState
        }.catch { error ->
            emit(WidgetUIState.Error(error.message ?: error.toString()))
        }
}
