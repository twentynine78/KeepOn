package fr.twentynine.keepon.ui.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.data.mapper.ScreenTimeoutToScreenTimeoutUIMapper
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TaskerEditUIState
import fr.twentynine.keepon.data.model.TaskerUIEvent
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaskerEditViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {

    private val selectedScreenTimeoutUI: MutableStateFlow<ScreenTimeoutUI?> = MutableStateFlow(null)

    private lateinit var uiStateFlow: StateFlow<TaskerEditUIState>

    private lateinit var systemSettingPermissionManager: SystemSettingPermissionManager
    private lateinit var postNotificationPermissionManager: PostNotificationPermissionManager
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager

    private lateinit var managedActivityResultLauncher: ManagedActivityResultLauncher<String, Boolean>

    fun setManagedActivityResultLauncher(activityResultLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        managedActivityResultLauncher = activityResultLauncher
    }

    fun initViewModel(
        systemSettingPermissionManager: SystemSettingPermissionManager,
        postNotificationPermissionManager: PostNotificationPermissionManager,
        batteryOptimizationManager: BatteryOptimizationManager,
    ) {
        this.systemSettingPermissionManager = systemSettingPermissionManager
        this.postNotificationPermissionManager = postNotificationPermissionManager
        this.batteryOptimizationManager = batteryOptimizationManager
    }

    suspend fun getUiState(): StateFlow<TaskerEditUIState> {
        return withContext(Dispatchers.IO) {
            uiStateFlow = getCombinedFlow()
                .catch { error ->
                    TaskerEditUIState.Error(error.message ?: error.toString())
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    TaskerEditUIState.Loading
                )

            return@withContext uiStateFlow
        }
    }

    private suspend fun getCombinedFlow(): Flow<TaskerEditUIState.Success> {
        return combine(
            systemSettingPermissionManager.canWriteSystemSetting,
            batteryOptimizationManager.batteryIsNotOptimized,
            postNotificationPermissionManager.canPostNotification,
            userPreferencesRepository.getDefaultScreenTimeoutFlow(),
            userPreferencesRepository.getPreviousScreenTimeoutFlow(),
            userPreferencesRepository.getTimeoutIconStyleFlow(),
            selectedScreenTimeoutUI,
        ) { arrayOfFlow ->

            val maxAllowedScreenTimeout = userPreferencesRepository.getMaxAllowedScreenTimeout()

            val specialScreenTimeoutUI = userPreferencesRepository.specialScreenTimeouts
                .map { screenTimeout ->
                    ScreenTimeoutToScreenTimeoutUIMapper
                        .setStringResourceProvider(stringResourceProvider)
                        .map(screenTimeout)
                }
            val screenTimeoutUI = userPreferencesRepository.screenTimeouts
                .map { screenTimeout ->
                    ScreenTimeoutToScreenTimeoutUIMapper
                        .setStringResourceProvider(stringResourceProvider)
                        .setIsLocked(screenTimeout.value > maxAllowedScreenTimeout)
                        .map(screenTimeout)
                }

            TaskerEditUIState.Success(
                canWriteSystemSettings = arrayOfFlow[0] as Boolean,
                batteryIsNotOptimized = arrayOfFlow[1] as Boolean,
                canPostNotification = arrayOfFlow[2] as Boolean,
                defaultScreenTimeout = arrayOfFlow[3] as ScreenTimeout,
                previousScreenTimeout = arrayOfFlow[4] as ScreenTimeout,
                timeoutIconStyle = arrayOfFlow[5] as TimeoutIconStyle,
                specialScreenTimeouts = specialScreenTimeoutUI,
                screenTimeouts = screenTimeoutUI,
                selectedScreenTimeout = arrayOfFlow[6] as ScreenTimeoutUI?,
            )
        }
    }

    fun onEvent(event: TaskerUIEvent) {
        when (event) {
            TaskerUIEvent.RequestWriteSystemSettingPermission -> requestWriteSystemSettingsPermission()
            TaskerUIEvent.RequestDisableBatteryOptimization -> requestDisableBatteryOptimization()
            TaskerUIEvent.RequestPostNotification -> requestPostNotificationPermission()
            TaskerUIEvent.UpdateIsFirstLaunch -> updateIsFirstLaunch()
            TaskerUIEvent.CheckNeededPermissions -> checkNeededPermissions()
            is TaskerUIEvent.SetSelectedScreenTimeout -> setSelectedScreenTimeout(event.screenTimeoutUI)
        }
    }

    fun setInitialSelectedScreenTimeout(screenTimeout: Int) {
        viewModelScope.launch {
            val maxAllowedScreenTimeout = userPreferencesRepository.getMaxAllowedScreenTimeout()

            val specialScreenTimeoutUI = userPreferencesRepository.specialScreenTimeouts
                .map { screenTimeout ->
                    ScreenTimeoutToScreenTimeoutUIMapper
                        .setStringResourceProvider(stringResourceProvider)
                        .map(screenTimeout)
                }
            val screenTimeoutUI = userPreferencesRepository.screenTimeouts
                .map { screenTimeout ->
                    ScreenTimeoutToScreenTimeoutUIMapper
                        .setStringResourceProvider(stringResourceProvider)
                        .setIsLocked(screenTimeout.value > maxAllowedScreenTimeout)
                        .map(screenTimeout)
                }

            val allScreenTimeoutList = screenTimeoutUI
                .plus(specialScreenTimeoutUI)
            val initialScreenTimeout = allScreenTimeoutList
                .firstOrNull { screenTimeoutUI ->
                    screenTimeoutUI.value == screenTimeout
                }

            if (initialScreenTimeout != null && !initialScreenTimeout.isLocked) {
                setSelectedScreenTimeout(initialScreenTimeout)
            }
        }
    }

    fun getMaxAllowedScreenTimeout() = userPreferencesRepository.getMaxAllowedScreenTimeout()

    fun checkNeededPermissions() {
        viewModelScope.launch {
            awaitAll(
                async { checkWriteSystemSettingsPermission() },
                async { checkBatteryOptimizationState() }
            )
        }
    }

    fun checkWriteSystemSettingsPermission() {
        systemSettingPermissionManager.checkWriteSystemSettingsPermission()
    }

    fun checkBatteryOptimizationState() {
        batteryOptimizationManager.checkBatteryOptimizationState()
    }

    fun checkPostNotificationPermission() {
        postNotificationPermissionManager.checkPostNotificationPermission()
    }

    private fun setSelectedScreenTimeout(screenTimeout: ScreenTimeoutUI) {
        selectedScreenTimeoutUI.update { screenTimeout }
    }

    private fun requestWriteSystemSettingsPermission() {
        systemSettingPermissionManager.requestWriteSystemSettingsPermission()
    }

    private fun requestDisableBatteryOptimization() {
        batteryOptimizationManager.requestDisableBatteryOptimization()
    }

    private fun requestPostNotificationPermission() {
        postNotificationPermissionManager.requestPostNotificationPermission(
            managedActivityResultLauncher,
        )
    }

    fun updatePostNotificationPermission(canPostNotification: Boolean) {
        postNotificationPermissionManager.updatePostNotificationPermission(canPostNotification)
    }

    private fun updateIsFirstLaunch() {
        viewModelScope.launch {
            userPreferencesRepository.setIsFirstLaunch(false)
        }
    }
}
