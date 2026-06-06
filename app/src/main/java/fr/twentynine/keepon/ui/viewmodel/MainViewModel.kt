package fr.twentynine.keepon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.ui.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.ui.producer.MainViewStateProducer
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.gateway.AddTileServiceManager
import fr.twentynine.keepon.domain.gateway.AppRateManager
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.usecase.app.IncrementAppLaunchCountUseCase
import fr.twentynine.keepon.domain.usecase.app.SetIsFirstLaunchUseCase
import fr.twentynine.keepon.domain.usecase.preferences.DismissTipUseCase
import fr.twentynine.keepon.domain.usecase.preferences.SetQSTileAddedUseCase
import fr.twentynine.keepon.domain.usecase.preferences.SetResetTimeoutWhenScreenOffUseCase
import fr.twentynine.keepon.domain.usecase.preferences.UpdateTimeoutIconStyleUseCase
import fr.twentynine.keepon.domain.usecase.timeout.SetDefaultScreenTimeoutUseCase
import fr.twentynine.keepon.domain.usecase.timeout.SetNextSystemScreenTimeoutUseCase
import fr.twentynine.keepon.domain.usecase.timeout.ToggleScreenTimeoutSelectionUseCase
import kotlinx.coroutines.Dispatchers
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
    private val mainViewStateProducer: MainViewStateProducer,
    private val permissionStateGateway: PermissionStateGateway,
    private val appRateHelper: AppRateManager,
    private val addTileServiceManager: AddTileServiceManager,
    private val setNextSystemScreenTimeoutUseCase: SetNextSystemScreenTimeoutUseCase,
    private val setDefaultScreenTimeoutUseCase: SetDefaultScreenTimeoutUseCase,
    private val toggleScreenTimeoutSelectionUseCase: ToggleScreenTimeoutSelectionUseCase,
    private val setResetTimeoutWhenScreenOffUseCase: SetResetTimeoutWhenScreenOffUseCase,
    private val updateTimeoutIconStyleUseCase: UpdateTimeoutIconStyleUseCase,
    private val dismissTipUseCase: DismissTipUseCase,
    private val setQSTileAddedUseCase: SetQSTileAddedUseCase,
    private val incrementAppLaunchCountUseCase: IncrementAppLaunchCountUseCase,
    private val setIsFirstLaunchUseCase: SetIsFirstLaunchUseCase,
) : ViewModel() {

    private lateinit var uiStateFlow: StateFlow<MainViewUIState>

    private var appLaunchIncremented = false

    suspend fun getUiState(): StateFlow<MainViewUIState> {
        return withContext(Dispatchers.IO) {
            uiStateFlow = mainViewStateProducer(
                canWriteSystemSettingFlow = permissionStateGateway.canWriteSystemSetting,
                batteryIsNotOptimizedFlow = permissionStateGateway.batteryIsNotOptimized,
                canPostNotificationFlow = permissionStateGateway.canPostNotification,
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

    fun onEvent(event: MainUIEvent) {
        when (event) {
            MainUIEvent.SetNextSelectedSystemScreenTimeout -> setNextSelectedSystemScreenTimeout()
            MainUIEvent.UpdateIsFirstLaunch -> updateIsFirstLaunch()
            MainUIEvent.RequestAddTileService -> requestAddTileService()
            MainUIEvent.RequestAppRate -> requestAppRate()
            MainUIEvent.IncrementAppLaunchCount -> incrementAppLaunchCount()
            is MainUIEvent.SetResetTimeoutWhenScreenOff -> setResetTimeoutWhenScreenOff(event.resetTimeoutWhenScreenOff)
            is MainUIEvent.ToggleScreenTimeoutSelection -> toggleScreenTimeoutSelection(event.screenTimeoutUI)
            is MainUIEvent.SetDefaultScreenTimeout -> setDefaultScreenTimeout(event.timeout)
            is MainUIEvent.UpdateTimeoutIconStyle -> updateTimeoutIconStyle(event.timeoutIconStyle)
            is MainUIEvent.DismissTips -> setDismissedTips(event.tipsId)

            // Permission request/check events are handled by the host Activity.
            MainUIEvent.RequestWriteSystemSettingPermission,
            MainUIEvent.RequestDisableBatteryOptimization,
            MainUIEvent.RequestPostNotification,
            MainUIEvent.CheckNeededPermissions -> Unit
        }
    }

    fun incrementAppLaunchCount(runBlocking: Boolean = false) {
        val action = suspend {
            if (!appLaunchIncremented && permissionStateGateway.areRequiredPermissionsGranted()) {
                incrementAppLaunchCountUseCase()
                appLaunchIncremented = true
            }
        }
        if (runBlocking) {
            runBlocking { action() }
        } else {
            viewModelScope.launch { action() }
        }
    }

    private fun requestAddTileService() {
        addTileServiceManager.requestAddTileService(
            { setQSTileAdded() },
            {}
        )
    }

    private fun toggleScreenTimeoutSelection(screenTimeoutUI: ScreenTimeoutUI) {
        viewModelScope.launch {
            toggleScreenTimeoutSelectionUseCase(ScreenTimeoutUIToScreenTimeoutMapper.map(screenTimeoutUI))
        }
    }

    private fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean) {
        viewModelScope.launch {
            setResetTimeoutWhenScreenOffUseCase(resetWhenScreenOff)
        }
    }

    private fun setNextSelectedSystemScreenTimeout() {
        viewModelScope.launch {
            setNextSystemScreenTimeoutUseCase()
        }
    }

    private fun setDefaultScreenTimeout(newScreenTimeout: ScreenTimeout) {
        viewModelScope.launch {
            setDefaultScreenTimeoutUseCase(newScreenTimeout)
        }
    }

    private fun updateTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) {
        viewModelScope.launch {
            updateTimeoutIconStyleUseCase(timeoutIconStyle)
        }
    }

    private fun setQSTileAdded() {
        viewModelScope.launch {
            setQSTileAddedUseCase(true)
        }
    }

    private fun setDismissedTips(dismissedTipId: Int) {
        viewModelScope.launch {
            dismissTipUseCase(dismissedTipId)
        }
    }

    private fun updateIsFirstLaunch() {
        viewModelScope.launch {
            setIsFirstLaunchUseCase(false)
        }
    }

    private fun requestAppRate() {
        appRateHelper.openPlayStore()
        viewModelScope.launch {
            dismissTipUseCase(TipsInfo.RateApp.id)
        }
    }
}
