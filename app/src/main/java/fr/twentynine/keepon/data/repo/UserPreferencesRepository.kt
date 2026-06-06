package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.CURRENT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.data.migration.OldTimeoutIconStyle
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.util.timeout.DesiredScreenTimeoutController
import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UserPreferencesRepository {
    val screenTimeouts: List<ScreenTimeout>
    val specialScreenTimeouts: List<ScreenTimeout>

    suspend fun getKeepOnIsActive(): Boolean
    suspend fun getKeepOnIsActiveFlow(): Flow<Boolean>
    suspend fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getDefaultScreenTimeout(): ScreenTimeout
    suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout, checkService: Boolean = false)
    suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getCurrentScreenTimeout(): ScreenTimeout
    suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean = false)
    suspend fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>>
    suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout>
    suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>)
    suspend fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean>
    suspend fun getResetTimeoutWhenScreenOff(): Boolean
    suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean)
    suspend fun getOldResetTimeoutWhenScreenOff(): Boolean?
    suspend fun removeOldResetTimeoutWhenScreenOff()
    suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle>
    suspend fun getTimeoutIconStyle(): TimeoutIconStyle
    suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle)
    suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle?
    suspend fun removeOldTimeoutIconStyle()
    suspend fun getQSTileAddedFlow(): Flow<Boolean>
    suspend fun setQSTileAdded(isAdded: Boolean)
    suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun setNextSelectedSystemScreenTimeout(
        currentTimeout: ScreenTimeout? = null,
        invokeUpdateComponents: suspend () -> Unit
    )
    suspend fun setNewSystemScreenTimeout(
        newTimeout: ScreenTimeout,
        forceUpdatePreviousTimeout: Boolean = false,
        invokeUpdateComponents: suspend () -> Unit
    )
    suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>>
    suspend fun getOldSelectedScreenTimeouts(): String
    suspend fun removeOldSelectedScreenTimeouts()
    suspend fun getOldAppReviewAsked(): Boolean
    suspend fun removeOldAppReviewAsked()
    suspend fun getOldSkipIntro(): Boolean
    suspend fun removeOldSkipIntro()
    suspend fun getIsFirstLaunchFlow(): Flow<Boolean>
    suspend fun setIsFirstLaunch(isFirstLaunch: Boolean)
    suspend fun getAppLaunchCountFlow(): Flow<Long>
    suspend fun getAppLaunchCount(): Long
    suspend fun setAppLaunchCount(appLaunchCount: Long)
    suspend fun resetSystemScreenTimeoutToDefault(invokeUpdateComponents: suspend () -> Unit)
    suspend fun setDismissedTip(dismissedTips: DismissedTips)
    suspend fun getLastRunVersionCode(): Long
    suspend fun setLastRunVersionCode(versionCode: Long)
    fun getMaxAllowedScreenTimeout(): Long
}

class UserPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val systemScreenTimeoutController: dagger.Lazy<SystemScreenTimeoutController>,
    private val devicePolicyManagerHelper: dagger.Lazy<DevicePolicyController>,
    private val screenOffReceiverServiceManager: dagger.Lazy<ScreenOffReceiverServiceManager>,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
) : UserPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override val screenTimeouts = ScreenTimeoutCatalog.screenTimeouts
    override val specialScreenTimeouts = ScreenTimeoutCatalog.specialScreenTimeouts

    override suspend fun getKeepOnIsActive(): Boolean = getKeepOnIsActiveFlow().firstOrNull() ?: false

    override suspend fun getKeepOnIsActiveFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            combine(
                getCurrentScreenTimeoutFlow(),
                getDefaultScreenTimeoutFlow(),
                getResetTimeoutWhenScreenOffFlow(),
            ) { currentTimeout, defaultTimeout, resetTimeoutWhenScreenOff ->
                resetTimeoutWhenScreenOff && currentTimeout != defaultTimeout
            }
        }

    override suspend fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout> =
        timeoutPreferencesRepository.getDefaultScreenTimeoutFlow()

    override suspend fun getDefaultScreenTimeout(): ScreenTimeout =
        timeoutPreferencesRepository.getDefaultScreenTimeout()

    override suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout, checkService: Boolean): Unit =
        withContext(ioDispatcher) {
            timeoutPreferencesRepository.setDefaultScreenTimeout(timeout)
            if (checkService && timeout == getCurrentScreenTimeout()) {
                screenOffReceiverServiceManager.get().stopService()
            }
        }

    override suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> =
        timeoutPreferencesRepository.getCurrentScreenTimeoutFlow()

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout =
        timeoutPreferencesRepository.getCurrentScreenTimeout()

    override suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean) =
        withContext(ioDispatcher) {
            val currentTimeout = getCurrentScreenTimeout()

            if (currentTimeout != timeout || forceUpdatePreviousTimeout) {
                setPreviousScreenTimeout(currentTimeout)
            }

            if (getResetTimeoutWhenScreenOff() && timeout != getDefaultScreenTimeout()) {
                screenOffReceiverServiceManager.get().startService()
            } else {
                screenOffReceiverServiceManager.get().stopService()
            }

            preferenceDataStoreHelper.putPreference(
                CURRENT_SCREEN_TIMEOUT,
                timeout.value,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override suspend fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>> =
        timeoutPreferencesRepository.getSelectedScreenTimeoutFlow()

    override suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout> =
        timeoutPreferencesRepository.getSelectedScreenTimeouts()

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        timeoutPreferencesRepository.setSelectedScreenTimeouts(selectedTimeouts)

    override suspend fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean> =
        timeoutPreferencesRepository.getResetTimeoutWhenScreenOffFlow()

    override suspend fun getResetTimeoutWhenScreenOff(): Boolean =
        timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()

    override suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean) =
        withContext(ioDispatcher) {
            if (!resetWhenScreenOff) {
                screenOffReceiverServiceManager.get().stopService()
                setDefaultScreenTimeout(getCurrentScreenTimeout())
            } else if (getCurrentScreenTimeout() != getDefaultScreenTimeout()) {
                screenOffReceiverServiceManager.get().startService()
            }

            preferenceDataStoreHelper.putPreference(
                RESET_TIMEOUT_WHEN_SCREEN_OFF,
                resetWhenScreenOff,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldResetTimeoutWhenScreenOff(): Boolean? =
        legacyPreferencesRepository.getOldResetTimeoutWhenScreenOff()

    override suspend fun removeOldResetTimeoutWhenScreenOff() =
        legacyPreferencesRepository.removeOldResetTimeoutWhenScreenOff()

    override suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> =
        uiPreferencesRepository.getTimeoutIconStyleFlow()

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle =
        uiPreferencesRepository.getTimeoutIconStyle()

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        uiPreferencesRepository.setTimeoutIconStyle(timeoutIconStyle)

    override suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle? =
        legacyPreferencesRepository.getOldTimeoutIconStyle()

    override suspend fun removeOldTimeoutIconStyle() =
        legacyPreferencesRepository.removeOldTimeoutIconStyle()

    override suspend fun getQSTileAddedFlow(): Flow<Boolean> =
        uiPreferencesRepository.getQSTileAddedFlow()

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        uiPreferencesRepository.setQSTileAdded(isAdded)

    override suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> =
        timeoutPreferencesRepository.getPreviousScreenTimeoutFlow()

    private suspend fun getPreviousScreenTimeout(): ScreenTimeout =
        timeoutPreferencesRepository.getPreviousScreenTimeout()

    private suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        timeoutPreferencesRepository.setPreviousScreenTimeout(timeout)

    override suspend fun setNextSelectedSystemScreenTimeout(currentTimeout: ScreenTimeout?, invokeUpdateComponents: suspend () -> Unit) =
        withContext(ioDispatcher) {
            val currentTimeout = currentTimeout ?: getCurrentScreenTimeout()
            val nextTimeoutValue = getNextSelectedScreenTimeout(currentTimeout)
            val defaultTimeout = getDefaultScreenTimeout()

            if (!devicePolicyManagerHelper.get().isValidTimeout(nextTimeoutValue)) {
                return@withContext
            }

            setSystemScreenTimeout(nextTimeoutValue) { invokeUpdateComponents() }

            updateDefaultScreenTimeoutIfNoResetTimeout(
                currentTimeout,
                defaultTimeout,
                nextTimeoutValue
            )
        }

    override suspend fun setNewSystemScreenTimeout(newTimeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean, invokeUpdateComponents: suspend () -> Unit) =
        withContext(ioDispatcher) {
            val defaultTimeout = getDefaultScreenTimeout()
            val currentTimeout = getCurrentScreenTimeout()
            val previousTimeout = getPreviousScreenTimeout()

            val timeout = when (newTimeout.value) {
                SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value -> defaultTimeout
                SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value -> previousTimeout
                else -> newTimeout
            }

            if (!devicePolicyManagerHelper.get().isValidTimeout(timeout)) {
                return@withContext
            }

            setSystemScreenTimeout(timeout, forceUpdatePreviousTimeout) { invokeUpdateComponents() }

            updateDefaultScreenTimeoutIfNoResetTimeout(
                currentTimeout,
                defaultTimeout,
                newTimeout
            )
        }

    private suspend fun updateDefaultScreenTimeoutIfNoResetTimeout(
        currentTimeout: ScreenTimeout,
        defaultTimeout: ScreenTimeout,
        newTimeout: ScreenTimeout
    ) =
        withContext(ioDispatcher) {
            if (!getResetTimeoutWhenScreenOff()) {
                // Check if is needed to add the current timeout to selected timeouts
                if (currentTimeout == defaultTimeout) {
                    val selectedTimeouts = getSelectedScreenTimeouts()
                    val currentIsSelected = selectedTimeouts.contains(currentTimeout)
                    if (!currentIsSelected) {
                        setSelectedScreenTimeouts(selectedTimeouts.plus(currentTimeout))
                    }
                }
                // Set the new default timeout
                setDefaultScreenTimeout(newTimeout)
            }
        }

    override suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>> =
        uiPreferencesRepository.getDismissedTipsFlow()

    override suspend fun setDismissedTip(dismissedTips: DismissedTips) =
        uiPreferencesRepository.setDismissedTip(dismissedTips)

    override suspend fun getOldSelectedScreenTimeouts(): String =
        legacyPreferencesRepository.getOldSelectedScreenTimeouts()

    override suspend fun removeOldSelectedScreenTimeouts() =
        legacyPreferencesRepository.removeOldSelectedScreenTimeouts()

    override suspend fun getOldAppReviewAsked(): Boolean =
        legacyPreferencesRepository.getOldAppReviewAsked()

    override suspend fun removeOldAppReviewAsked() =
        legacyPreferencesRepository.removeOldAppReviewAsked()

    override suspend fun getOldSkipIntro(): Boolean =
        legacyPreferencesRepository.getOldSkipIntro()

    override suspend fun removeOldSkipIntro() =
        legacyPreferencesRepository.removeOldSkipIntro()

    override suspend fun getIsFirstLaunchFlow(): Flow<Boolean> =
        appPreferencesRepository.getIsFirstLaunchFlow()

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        appPreferencesRepository.setIsFirstLaunch(isFirstLaunch)

    override suspend fun getAppLaunchCountFlow(): Flow<Long> =
        appPreferencesRepository.getAppLaunchCountFlow()

    override suspend fun getAppLaunchCount(): Long =
        appPreferencesRepository.getAppLaunchCount()

    override suspend fun setAppLaunchCount(appLaunchCount: Long) =
        appPreferencesRepository.setAppLaunchCount(appLaunchCount)

    override suspend fun resetSystemScreenTimeoutToDefault(invokeUpdateComponents: suspend () -> Unit) =
        withContext(ioDispatcher) {
            val defaultTimeout = getDefaultScreenTimeout()
            setSystemScreenTimeout(
                defaultTimeout
            ) { invokeUpdateComponents() }
            screenOffReceiverServiceManager.get().stopService()
        }

    private suspend fun getNextSelectedScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout =
        withContext(ioDispatcher) {
            val screenTimeouts = getSelectedScreenTimeouts().toMutableSet()
            val defaultScreenTimeout = getDefaultScreenTimeout()

            screenTimeouts.addAll(
                devicePolicyManagerHelper.get().removeNotAllowedScreenTimeout(
                    listOf(defaultScreenTimeout, currentTimeout)
                )
            )

            val sortedScreenTimeouts = screenTimeouts.sortedBy { it.value }.distinct()
            if (sortedScreenTimeouts.isEmpty()) {
                return@withContext currentTimeout
            }
            val nextIndex = sortedScreenTimeouts.indexOfFirst { it.value == currentTimeout.value }.let { currentIndex ->
                if (currentIndex == -1 || currentIndex == sortedScreenTimeouts.size - 1) 0 else currentIndex + 1
            }
            sortedScreenTimeouts[nextIndex]
        }

    override suspend fun getLastRunVersionCode(): Long =
        appPreferencesRepository.getLastRunVersionCode()

    override suspend fun setLastRunVersionCode(versionCode: Long) =
        appPreferencesRepository.setLastRunVersionCode(versionCode)

    override fun getMaxAllowedScreenTimeout() = devicePolicyManagerHelper.get().getMaxAllowedScreenTimeout()

    private suspend fun setSystemScreenTimeout(
        timeout: ScreenTimeout,
        forceUpdatePreviousTimeout: Boolean = false,
        invokeUpdateComponents: suspend () -> Unit,
    ) = withContext(ioDispatcher) {
        // Check if timeout value is allowed
        if (!devicePolicyManagerHelper.get().isValidTimeout(timeout)) {
            return@withContext
        }

        // Update flow directly to prevent lag in UI (value will be override by the worker)
        setCurrentScreenTimeout(timeout, forceUpdatePreviousTimeout)
        invokeUpdateComponents()

        // Set the desired timeout and update the system screen timeout
        DesiredScreenTimeoutController.setDesiredScreenTimeout(timeout, systemScreenTimeoutController.get())
    }
}
