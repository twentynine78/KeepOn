package fr.twentynine.keepon.ui.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.data.local.TipsInfo
import fr.twentynine.keepon.data.mapper.ScreenTimeoutToScreenTimeoutUIMapper
import fr.twentynine.keepon.data.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.data.model.DismissedTips
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.data.model.TipsConstraintState
import fr.twentynine.keepon.data.repo.TipsInfoRepository
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.AddTileServiceManager
import fr.twentynine.keepon.util.AppRateHelper
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import fr.twentynine.keepon.util.coil.MemoryCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val stringResourceProvider: StringResourceProvider,
    private val appRateHelper: AppRateHelper,
    private val addTileServiceManager: AddTileServiceManager,
    private val memoryCacheManager: MemoryCacheManager,
    private val qsTileUpdater: QSTileUpdater,
) : ViewModel() {

    private val tipsConstraintState = MutableStateFlow(TipsConstraintState())

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
            uiStateFlow = getCombinedFlow()
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

    private suspend fun combinedTipsList(): Flow<List<DismissedTips>> {
        return combine(
            userPreferencesRepository.getDismissedTipsFlow(),
            postNotificationPermissionManager.canPostNotification,
            batteryOptimizationManager.batteryIsNotOptimized,
            userPreferencesRepository.getQSTileAddedFlow(),
            userPreferencesRepository.getAppLaunchCountFlow(),
        ) { flowArray ->
            tipsConstraintState.update {
                tipsConstraintState.value.copy(
                    canPostNotification = flowArray[1] as Boolean,
                    batteryIsNotOptimized = flowArray[2] as Boolean,
                    tileServiceIsAdded = flowArray[3] as Boolean,
                    showRateApp = appRateHelper.needShowRateTip(
                        flowArray[4] as Long,
                        appRateHelper.getFirstInstallTime(),
                        appRateHelper.canRateApp()
                    ),
                )
            }

            @Suppress("UNCHECKED_CAST")
            flowArray[0] as List<DismissedTips>
        }
    }

    private suspend fun combinedScreenTimeoutList(): Flow<List<ScreenTimeoutUI>> {
        return combine(
            flowOf(userPreferencesRepository.screenTimeouts),
            userPreferencesRepository.getSelectedScreenTimeoutFlow(),
            userPreferencesRepository.getDefaultScreenTimeoutFlow(),
            userPreferencesRepository.getCurrentScreenTimeoutFlow()
        ) { allScreenTimeout, selectedScreenTimeout, defaultScreenTimeout, currentScreenTimeout ->
            val maxAllowedScreenTimeout = userPreferencesRepository.getMaxAllowedScreenTimeout()

            allScreenTimeout.map { screenTimeout ->
                val isSelected = (
                    selectedScreenTimeout.contains(screenTimeout) &&
                        screenTimeout.value <= maxAllowedScreenTimeout
                    ) ||
                    screenTimeout == defaultScreenTimeout
                val isDefault = screenTimeout == defaultScreenTimeout
                val isCurrent = screenTimeout == currentScreenTimeout
                val isLocked = screenTimeout.value > maxAllowedScreenTimeout

                ScreenTimeoutToScreenTimeoutUIMapper
                    .setStringResourceProvider(stringResourceProvider)
                    .setIsSelected(isSelected)
                    .setIsDefault(isDefault)
                    .setIsCurrent(isCurrent)
                    .setIsLocked(isLocked)
                    .map(screenTimeout)
            }
        }
    }

    private suspend fun getCombinedFlow(): Flow<MainViewUIState.Success> {
        return combine(
            systemSettingPermissionManager.canWriteSystemSetting,
            batteryOptimizationManager.batteryIsNotOptimized,
            userPreferencesRepository.getResetTimeoutWhenScreenOffFlow(),
            userPreferencesRepository.getCurrentScreenTimeoutFlow(),
            userPreferencesRepository.getKeepOnIsActiveFlow(),
            userPreferencesRepository.getIsFirstLaunchFlow(),
            userPreferencesRepository.getTimeoutIconStyleFlow(),
            combinedTipsList(),
            combinedScreenTimeoutList(),
            postNotificationPermissionManager.canPostNotification,
        ) { arrayOfFlow ->
            @Suppress("UNCHECKED_CAST")
            MainViewUIState.Success(
                canWriteSystemSettings = arrayOfFlow[0] as Boolean,
                batteryIsNotOptimized = arrayOfFlow[1] as Boolean,
                resetTimeoutWhenScreenOff = arrayOfFlow[2] as Boolean,
                currentScreenTimeout = arrayOfFlow[3] as ScreenTimeout,
                keepOnIsActive = arrayOfFlow[4] as Boolean,
                isFirstLaunch = arrayOfFlow[5] as Boolean,
                timeoutIconStyle = arrayOfFlow[6] as TimeoutIconStyle,
                tipsList = TipsInfoRepository.tipsInfoList
                    .filter { tipsInfo ->
                        !(arrayOfFlow[7] as List<*>).contains(DismissedTips(tipsInfo.id)) &&
                            tipsInfo.constraint(tipsConstraintState.value)
                    },
                screenTimeouts = arrayOfFlow[8] as List<ScreenTimeoutUI>,
                canPostNotification = arrayOfFlow[9] as Boolean,
            )
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
                appRateHelper.incrementAppLaunchCount(userPreferencesRepository)
                appLaunchIncremented = true

                if (userPreferencesRepository.getAppLaunchCount() > 1L) {
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
            userPreferencesRepository.setNextSelectedSystemScreenTimeout { qsTileUpdater.requestUpdate() }
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
                if (newScreenTimeout == currentScreenTimeout) {
                    qsTileUpdater.requestUpdate()
                }

                if (defaultTimeout == currentScreenTimeout) {
                    userPreferencesRepository.setCurrentScreenTimeout(newScreenTimeout)
                    userPreferencesRepository.setNewSystemScreenTimeout(
                        newScreenTimeout
                    ) { qsTileUpdater.requestUpdate() }
                }
            }
        }
    }

    private fun updateTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) {
        viewModelScope.launch {
            memoryCacheManager.clear()
            userPreferencesRepository.setTimeoutIconStyle(timeoutIconStyle)
            qsTileUpdater.requestUpdate()
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
