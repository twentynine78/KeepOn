package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.state.TaskerEditUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Composes the Tasker edit UI state. The permission flows and the selected-timeout
 * flow are passed in as parameters because they are owned by the Activity-scoped
 * ViewModel and cannot be held by this Singleton-scoped producer.
 */
class TaskerEditStateProducer @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val buildScreenTimeoutUiListProducer: BuildScreenTimeoutUiListProducer,
) {
    suspend operator fun invoke(
        canWriteSystemSettingFlow: Flow<Boolean>,
        batteryIsNotOptimizedFlow: Flow<Boolean>,
        canPostNotificationFlow: Flow<Boolean>,
        selectedScreenTimeoutFlow: Flow<ScreenTimeoutUI?>,
    ): Flow<TaskerEditUIState.Success> {
        return combine(
            canWriteSystemSettingFlow,
            batteryIsNotOptimizedFlow,
            canPostNotificationFlow,
            timeoutPreferencesRepository.getDefaultScreenTimeoutFlow(),
            timeoutPreferencesRepository.getPreviousScreenTimeoutFlow(),
            uiPreferencesRepository.getTimeoutIconStyleFlow(),
            selectedScreenTimeoutFlow,
        ) { arrayOfFlow ->
            TaskerEditUIState.Success(
                canWriteSystemSettings = arrayOfFlow[0] as Boolean,
                batteryIsNotOptimized = arrayOfFlow[1] as Boolean,
                canPostNotification = arrayOfFlow[2] as Boolean,
                defaultScreenTimeout = arrayOfFlow[3] as ScreenTimeout,
                previousScreenTimeout = arrayOfFlow[4] as ScreenTimeout,
                timeoutIconStyle = arrayOfFlow[5] as TimeoutIconStyle,
                specialScreenTimeouts = buildScreenTimeoutUiListProducer(ScreenTimeoutCatalog.specialScreenTimeouts),
                screenTimeouts = buildScreenTimeoutUiListProducer(ScreenTimeoutCatalog.screenTimeouts),
                selectedScreenTimeout = arrayOfFlow[6] as ScreenTimeoutUI?,
            )
        }
    }
}
