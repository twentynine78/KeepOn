package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.ui.catalog.CreditLabelCatalog
import fr.twentynine.keepon.ui.catalog.IconTransitionLabelCatalog
import fr.twentynine.keepon.ui.catalog.TipsCatalog
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.domain.catalog.CreditCatalog
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.TipsConstraintState
import fr.twentynine.keepon.domain.gateway.AppInfoProvider
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.CheckIfRateTipNeededUseCase
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.ui.model.CreditInfoUI
import fr.twentynine.keepon.ui.model.CreditSectionUI
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.state.FirstLaunchHintGate
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
    private val firstLaunchHintGate: FirstLaunchHintGate,
) {
    // App metadata is static; read it once and reuse it across state emissions.
    private val appInfo by lazy { appInfoProvider.getAppInfo() }

    // The transition options (catalog set + order, with their labels resolved through the provider)
    // don't depend on any flow, so resolve them once and reuse the same instance across emissions.
    private val iconTransitionOptions by lazy {
        IconTransitionCatalog.all.map { transition ->
            IconTransitionOptionUI(
                id = transition.id,
                label = stringResourceProvider.getString(
                    IconTransitionLabelCatalog.labelResFor(transition.id)
                ),
            )
        }
    }

    // The About-screen credits are static too; resolve their labels (section names + versions) once.
    private val creditSections by lazy {
        CreditCatalog.creditInfoMap.map { (type, credits) ->
            CreditSectionUI(
                typeName = stringResourceProvider.getString(CreditLabelCatalog.typeNameResFor(type)),
                credits = credits.map { credit ->
                    CreditInfoUI(
                        name = credit.name,
                        author = credit.author,
                        url = credit.url,
                        version = CreditLabelCatalog.versionResFor(credit)
                            ?.let { stringResourceProvider.getString(it) },
                    )
                },
            )
        }
    }

    operator fun invoke(
        canWriteSystemSettingFlow: Flow<Boolean>,
        batteryIsNotOptimizedFlow: Flow<Boolean>,
        canPostNotificationFlow: Flow<Boolean>,
    ): Flow<MainViewUIState.Success> {
        // combine is only typed up to 5 flows; group into typed sub-combines (≤5 each) to keep
        // the whole pipeline type-safe instead of falling back to the Array overload + casts.
        val permissionFlagsFlow = combine(
            canWriteSystemSettingFlow,
            batteryIsNotOptimizedFlow,
            canPostNotificationFlow,
        ) { canWrite, battery, canPost ->
            PermissionFlags(canWrite, battery, canPost)
        }

        // Merge the two icon-presentation flows so the outer combine stays within its 5-flow
        // typed arity while feeding both the icon style and the transition config.
        val iconPresentationFlow = combine(
            uiPreferencesRepository.getTimeoutIconStyleFlow(),
            uiPreferencesRepository.getIconTransitionAnimationFlow(),
        ) { iconStyle, transition -> iconStyle to transition }

        // The first-launch swipe hint must play at most once per process: gate the persisted
        // flag with the session state, so re-enabling the reset option or recreating the
        // activity cannot replay it.
        val showFirstLaunchHintFlow = combine(
            appPreferencesRepository.getIsFirstLaunchFlow(),
            firstLaunchHintGate.hintPlayed,
        ) { isFirstLaunch, hintPlayed -> isFirstLaunch && !hintPlayed }

        val mainPreferencesFlow = combine(
            timeoutPreferencesRepository.getResetTimeoutWhenScreenOffFlow(),
            timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
            getKeepOnStatusUseCase(),
            showFirstLaunchHintFlow,
            iconPresentationFlow,
        ) { reset, current, keepOnIsActive, showFirstLaunchHint, (iconStyle, transition) ->
            MainPreferences(reset, current, keepOnIsActive, showFirstLaunchHint, iconStyle, transition)
        }

        return combine(
            permissionFlagsFlow,
            mainPreferencesFlow,
            tipsListFlow(canPostNotificationFlow, batteryIsNotOptimizedFlow),
            screenTimeoutListFlow(),
        ) { permissions, preferences, tipsList, screenTimeouts ->
            MainViewUIState.Success(
                canWriteSystemSettings = permissions.canWriteSystemSettings,
                batteryIsNotOptimized = permissions.batteryIsNotOptimized,
                canPostNotification = permissions.canPostNotification,
                resetTimeoutWhenScreenOff = preferences.resetTimeoutWhenScreenOff,
                currentScreenTimeout = preferences.currentScreenTimeout,
                currentTimeoutDisplay = preferences.currentScreenTimeout
                    .getFullDisplayTimeout(stringResourceProvider),
                keepOnIsActive = preferences.keepOnIsActive,
                showFirstLaunchHint = preferences.showFirstLaunchHint,
                timeoutIconStyle = preferences.timeoutIconStyle,
                iconTransitionAnimation = preferences.iconTransitionAnimation,
                iconTransitionOptions = iconTransitionOptions,
                tipsList = tipsList,
                screenTimeouts = screenTimeouts,
                appInfo = appInfo,
                creditSections = creditSections,
            )
        }
    }

    private fun tipsListFlow(
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

    private fun screenTimeoutListFlow(): Flow<List<ScreenTimeoutUI>> {
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
