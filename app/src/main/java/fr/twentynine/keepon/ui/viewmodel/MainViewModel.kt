package fr.twentynine.keepon.ui.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.data.catalog.TipsInfo
import fr.twentynine.keepon.ui.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.ui.producer.MainViewStateProducer
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.ui.components.AddTileServiceManager
import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.AppRateManager
import fr.twentynine.keepon.util.permission.BatteryOptimizationManager
import fr.twentynine.keepon.util.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.util.permission.SystemSettingPermissionManager
import fr.twentynine.keepon.domain.gateway.MemoryCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mainViewStateProducer: MainViewStateProducer,
    private val appRateHelper: AppRateManager,
    private val addTileServiceManager: AddTileServiceManager,
    private val memoryCacheManager: MemoryCacheManager,
    private val appComponentsUpdater: AppComponentsUpdater,
) : ViewModel() {

    private lateinit var uiStateFlow: StateFlow<MainViewUIState>

    private lateinit var systemSettingPermissionManager: SystemSettingPermissionManager
    private lateinit var postNotificationPermissionManager: PostNotificationPermissionManager
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager

    private lateinit var managedActivityResultLauncher: ManagedActivityResultLauncher<String, Boolean>

    private var appLaunchIncremented = false

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

    suspend fun getUiState(): StateFlow<MainViewUIState> {
        return withContext(Dispatchers.IO) {
            uiStateFlow = mainViewStateProducer(
                canWriteSystemSettingFlow = systemSettingPermissionManager.canWriteSystemSetting,
                batteryIsNotOptimizedFlow = batteryOptimizationManager.batteryIsNotOptimized,
                canPostNotificationFlow = postNotificationPermissionManager.canPostNotification,
            )
                .catch { error ->
                    MainViewUIState.Error(error.message ?: error.toString())
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    MainViewUIState.Loading
                )

            return@withContext uiStateFlow
        }
    }

    private fun isPermissionsGranted(): Boolean {
        return batteryOptimizationManager.isBatteryNotOptimized() &&
            systemSettingPermissionManager.canWriteSystemSettings()
    }

    fun onEvent(event: MainUIEvent) {
        when (event) {
            MainUIEvent.RequestWriteSystemSettingPermission -> requestWriteSystemSettingsPermission()
            MainUIEvent.RequestDisableBatteryOptimization -> requestDisableBatteryOptimization()
            MainUIEvent.SetNextSelectedSystemScreenTimeout -> setNextSelectedSystemScreenTimeout()
            MainUIEvent.UpdateIsFirstLaunch -> updateIsFirstLaunch()
            MainUIEvent.RequestPostNotification -> requestPostNotificationPermission()
            MainUIEvent.RequestAddTileService -> requestAddTileService()
            MainUIEvent.RequestAppRate -> requestAppRate()
            MainUIEvent.CheckNeededPermissions -> checkNeededPermissions()
            MainUIEvent.IncrementAppLaunchCount -> incrementAppLaunchCount()
            is MainUIEvent.SetResetTimeoutWhenScreenOff -> setResetTimeoutWhenScreenOff(event.resetTimeoutWhenScreenOff)
            is MainUIEvent.ToggleScreenTimeoutSelection -> toggleScreenTimeoutSelection(event.screenTimeoutUI)
            is MainUIEvent.SetDefaultScreenTimeout -> setDefaultScreenTimeout(event.timeout)
            is MainUIEvent.UpdateTimeoutIconStyle -> updateTimeoutIconStyle(event.timeoutIconStyle)
            is MainUIEvent.DismissTips -> setDismissedTips(event.tipsId)
        }
    }

    fun updatePostNotificationPermission(canPostNotification: Boolean) {
        postNotificationPermissionManager.updatePostNotificationPermission(canPostNotification)
    }

    fun incrementAppLaunchCount(runBlocking: Boolean = false) {
        val action = suspend {
            if (!appLaunchIncremented && isPermissionsGranted()) {
                val currentCount = userPreferencesRepository.getAppLaunchCount()

                userPreferencesRepository.setAppLaunchCount(currentCount + 1)
                appLaunchIncremented = true

                if (currentCount > 1L) {
                    updateIsFirstLaunch()
                }
            }
        }
        if (runBlocking) {
            runBlocking { action() }
        } else {
            viewModelScope.launch { action() }
        }
    }

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

    private fun requestAddTileService() {
        addTileServiceManager.requestAddTileService(
            { setQSTileAdded() },
            {}
        )
    }

    private fun toggleScreenTimeoutSelection(screenTimeoutUI: ScreenTimeoutUI) {
        viewModelScope.launch {
            val defaultScreenTimeout = userPreferencesRepository.getDefaultScreenTimeout()

            if (screenTimeoutUI.value == defaultScreenTimeout.value) {
                return@launch
            }

            val screenTimeout = ScreenTimeoutUIToScreenTimeoutMapper.map(screenTimeoutUI)
            val currentSelection = userPreferencesRepository.getSelectedScreenTimeouts()

            userPreferencesRepository.setSelectedScreenTimeouts(
                if (currentSelection.contains(screenTimeout)) {
                    currentSelection.minus(screenTimeout)
                } else {
                    currentSelection.plus(screenTimeout)
                }
            )
        }
    }

    private fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setResetTimeoutWhenScreenOff(resetWhenScreenOff)
        }
    }

    private fun setNextSelectedSystemScreenTimeout() {
        viewModelScope.launch {
            userPreferencesRepository.setNextSelectedSystemScreenTimeout { appComponentsUpdater.requestUpdate() }
        }
    }

    private fun setDefaultScreenTimeout(
        newScreenTimeout: ScreenTimeout
    ) {
        viewModelScope.launch {
            val defaultTimeout = userPreferencesRepository.getDefaultScreenTimeout()
            val currentScreenTimeout = userPreferencesRepository.getCurrentScreenTimeout()

            if (newScreenTimeout != defaultTimeout) {
                userPreferencesRepository.setDefaultScreenTimeout(newScreenTimeout, true)
                if (defaultTimeout == currentScreenTimeout) {
                    userPreferencesRepository.setCurrentScreenTimeout(newScreenTimeout)
                    userPreferencesRepository.setNewSystemScreenTimeout(newScreenTimeout) {
                        appComponentsUpdater.requestUpdate()
                    }
                }
            }
            appComponentsUpdater.requestUpdate()
        }
    }

    private fun updateTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) {
        viewModelScope.launch {
            memoryCacheManager.clear()
            userPreferencesRepository.setTimeoutIconStyle(timeoutIconStyle)
            appComponentsUpdater.requestUpdate()
        }
    }

    private fun setQSTileAdded() {
        viewModelScope.launch {
            userPreferencesRepository.setQSTileAdded(true)
        }
    }

    private fun setDismissedTips(dismissedTipId: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDismissedTip(DismissedTips(dismissedTipId))
        }
    }

    private fun updateIsFirstLaunch() {
        viewModelScope.launch {
            userPreferencesRepository.setIsFirstLaunch(false)
        }
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

    private fun requestAppRate() {
        appRateHelper.openPlayStore()
        viewModelScope.launch {
            userPreferencesRepository.setDismissedTip(DismissedTips(TipsInfo.RateApp.id))
        }
    }
}
