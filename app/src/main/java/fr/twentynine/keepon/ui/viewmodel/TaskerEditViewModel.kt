package fr.twentynine.keepon.ui.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.state.TaskerEditUIState
import fr.twentynine.keepon.ui.event.TaskerUIEvent
import fr.twentynine.keepon.ui.producer.BuildScreenTimeoutUiListProducer
import fr.twentynine.keepon.ui.producer.TaskerEditStateProducer
import fr.twentynine.keepon.domain.usecase.app.SetIsFirstLaunchUseCase
import fr.twentynine.keepon.domain.usecase.timeout.GetMaxAllowedScreenTimeoutUseCase
import fr.twentynine.keepon.core.permission.BatteryOptimizationManager
import fr.twentynine.keepon.core.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.core.permission.SystemSettingPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaskerEditViewModel @Inject constructor(
    private val taskerEditStateProducer: TaskerEditStateProducer,
    private val buildScreenTimeoutUiListProducer: BuildScreenTimeoutUiListProducer,
    private val getMaxAllowedScreenTimeoutUseCase: GetMaxAllowedScreenTimeoutUseCase,
    private val setIsFirstLaunchUseCase: SetIsFirstLaunchUseCase,
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
            uiStateFlow = taskerEditStateProducer(
                canWriteSystemSettingFlow = systemSettingPermissionManager.canWriteSystemSetting,
                batteryIsNotOptimizedFlow = batteryOptimizationManager.batteryIsNotOptimized,
                canPostNotificationFlow = postNotificationPermissionManager.canPostNotification,
                selectedScreenTimeoutFlow = selectedScreenTimeoutUI,
            )
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
            val specialScreenTimeoutUI = buildScreenTimeoutUiListProducer(
                ScreenTimeoutCatalog.specialScreenTimeouts
            )
            val screenTimeoutUI = buildScreenTimeoutUiListProducer(
                ScreenTimeoutCatalog.screenTimeouts
            )

            val allScreenTimeoutList = screenTimeoutUI
                .plus(specialScreenTimeoutUI)
            val initialScreenTimeout = allScreenTimeoutList
                .firstOrNull { it.value == screenTimeout }

            initialScreenTimeout?.let { currentInitialTimeout ->
                val timeoutToSet = if (currentInitialTimeout.isLocked) {
                    allScreenTimeoutList.lastOrNull { !it.isLocked }
                } else {
                    currentInitialTimeout
                }
                timeoutToSet?.let { setSelectedScreenTimeout(it) }
            }
        }
    }

    fun getMaxAllowedScreenTimeout() = getMaxAllowedScreenTimeoutUseCase()

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
            setIsFirstLaunchUseCase(false)
        }
    }
}
