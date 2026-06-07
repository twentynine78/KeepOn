package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.ui.catalog.TipsCatalog
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.model.TipsConstraintState
import fr.twentynine.keepon.domain.gateway.AppInfoProvider
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.CheckIfRateTipNeededUseCase
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.state.MainViewUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Composes the main screen UI state. Permission flows are passed in as parameters
 * because the permission managers are Activity-scoped and cannot be held by this
 * Singleton-scoped producer.
 */
class MainViewStateProducer @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val getKeepOnStatusUseCase: GetKeepOnStatusUseCase,
    private val checkIfRateTipNeededUseCase: CheckIfRateTipNeededUseCase,
    private val buildScreenTimeoutUiListProducer: BuildScreenTimeoutUiListProducer,
    private val stringResourceProvider: StringResourceProvider,
    private val appInfoProvider: AppInfoProvider,
) {
    // App metadata is static; read it once and reuse it across state emissions.
    private val appInfo by lazy { appInfoProvider.getAppInfo() }

    suspend operator fun invoke(
        canWriteSystemSettingFlow: Flow<Boolean>,
        batteryIsNotOptimizedFlow: Flow<Boolean>,
        canPostNotificationFlow: Flow<Boolean>,
    ): Flow<MainViewUIState.Success> {
        return combine(
            canWriteSystemSettingFlow,
            batteryIsNotOptimizedFlow,
            canPostNotificationFlow,
            timeoutPreferencesRepository.getResetTimeoutWhenScreenOffFlow(),
            timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
            getKeepOnStatusUseCase(),
            appPreferencesRepository.getIsFirstLaunchFlow(),
            uiPreferencesRepository.getTimeoutIconStyleFlow(),
            tipsListFlow(canPostNotificationFlow, batteryIsNotOptimizedFlow),
            screenTimeoutListFlow(),
        ) { arrayOfFlow ->
            @Suppress("UNCHECKED_CAST")
            MainViewUIState.Success(
                canWriteSystemSettings = arrayOfFlow[0] as Boolean,
                batteryIsNotOptimized = arrayOfFlow[1] as Boolean,
                canPostNotification = arrayOfFlow[2] as Boolean,
                resetTimeoutWhenScreenOff = arrayOfFlow[3] as Boolean,
                currentScreenTimeout = arrayOfFlow[4] as ScreenTimeout,
                currentTimeoutDisplay = (arrayOfFlow[4] as ScreenTimeout)
                    .getFullDisplayTimeout(stringResourceProvider),
                keepOnIsActive = arrayOfFlow[5] as Boolean,
                isFirstLaunch = arrayOfFlow[6] as Boolean,
                timeoutIconStyle = arrayOfFlow[7] as TimeoutIconStyle,
                tipsList = arrayOfFlow[8] as List<TipsInfo>,
                screenTimeouts = arrayOfFlow[9] as List<ScreenTimeoutUI>,
                appInfo = appInfo,
            )
        }
    }

    private suspend fun tipsListFlow(
        canPostNotificationFlow: Flow<Boolean>,
        batteryIsNotOptimizedFlow: Flow<Boolean>,
    ): Flow<List<TipsInfo>> {
        return combine(
            uiPreferencesRepository.getDismissedTipsFlow(),
            canPostNotificationFlow,
            batteryIsNotOptimizedFlow,
            uiPreferencesRepository.getQSTileAddedFlow(),
            appPreferencesRepository.getAppLaunchCountFlow(),
        ) { dismissedTips, canPostNotification, batteryIsNotOptimized, tileServiceIsAdded, appLaunchCount ->
            val constraintState = TipsConstraintState(
                canPostNotification = canPostNotification,
                batteryIsNotOptimized = batteryIsNotOptimized,
                tileServiceIsAdded = tileServiceIsAdded,
                showRateApp = checkIfRateTipNeededUseCase(appLaunchCount),
            )

            TipsCatalog.tipsInfoList.filter { tipsInfo ->
                !dismissedTips.contains(DismissedTips(tipsInfo.id)) &&
                    tipsInfo.constraint(constraintState)
            }
        }
    }

    private suspend fun screenTimeoutListFlow(): Flow<List<ScreenTimeoutUI>> {
        return combine(
            timeoutPreferencesRepository.getSelectedScreenTimeoutFlow(),
            timeoutPreferencesRepository.getDefaultScreenTimeoutFlow(),
            timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
        ) { selectedScreenTimeout, defaultScreenTimeout, currentScreenTimeout ->
            buildScreenTimeoutUiListProducer(
                timeouts = ScreenTimeoutCatalog.screenTimeouts,
                selectedTimeouts = selectedScreenTimeout,
                defaultTimeout = defaultScreenTimeout,
                currentTimeout = currentScreenTimeout,
            )
        }
    }
}
