package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
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
        // combine is only typed up to 5 flows; group the permissions into a typed sub-combine to
        // keep the pipeline type-safe instead of the Array overload + casts.
        val permissionFlagsFlow = combine(
            canWriteSystemSettingFlow,
            batteryIsNotOptimizedFlow,
            canPostNotificationFlow,
        ) { canWrite, battery, canPost ->
            PermissionFlags(canWrite, battery, canPost)
        }

        return combine(
            permissionFlagsFlow,
            timeoutPreferencesRepository.getDefaultScreenTimeoutFlow(),
            timeoutPreferencesRepository.getPreviousScreenTimeoutFlow(),
            uiPreferencesRepository.getTimeoutIconStyleFlow(),
            selectedScreenTimeoutFlow,
        ) { permissions, defaultScreenTimeout, previousScreenTimeout, iconStyle, selectedScreenTimeout ->
            TaskerEditUIState.Success(
                canWriteSystemSettings = permissions.canWriteSystemSettings,
                batteryIsNotOptimized = permissions.batteryIsNotOptimized,
                canPostNotification = permissions.canPostNotification,
                defaultScreenTimeout = defaultScreenTimeout,
                previousScreenTimeout = previousScreenTimeout,
                timeoutIconStyle = iconStyle,
                specialScreenTimeouts = buildScreenTimeoutUiListProducer(ScreenTimeoutCatalog.specialScreenTimeouts),
                screenTimeouts = buildScreenTimeoutUiListProducer(ScreenTimeoutCatalog.screenTimeouts),
                selectedScreenTimeout = selectedScreenTimeout,
            )
        }
    }
}
