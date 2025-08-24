package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.APP_LAUNCH_COUNT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.CURRENT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DEFAULT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.IS_FIRST_LAUNCH
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.LAST_RUN_VERSION_CODE
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_APP_REVIEW_ASKED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SKIP_INTRO
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.PREVIOUS_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.QSTILE_ADDED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.model.DismissedTips
import fr.twentynine.keepon.data.model.OldTimeoutIconStyle
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.util.DataMigrationHelper
import fr.twentynine.keepon.util.DesiredScreenTimeoutController
import fr.twentynine.keepon.util.DevicePolicyManagerHelper
import fr.twentynine.keepon.util.SystemScreenTimeoutController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
    suspend fun setNextSelectedSystemScreenTimeout(requestUpdateQSTile: suspend () -> Unit)
    suspend fun setNewSystemScreenTimeout(
        newTimeout: ScreenTimeout,
        forceUpdatePreviousTimeout: Boolean = false,
        requestUpdateQSTile: () -> Unit
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
    suspend fun resetSystemScreenTimeoutToDefault(requestUpdateQSTile: () -> Unit)
    suspend fun setDismissedTip(dismissedTips: DismissedTips)
    suspend fun getLastRunVersionCode(): Long
    suspend fun setLastRunVersionCode(versionCode: Long)
    fun getMaxAllowedScreenTimeout(): Long
}

class UserPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val systemScreenTimeoutController: dagger.Lazy<SystemScreenTimeoutController>,
    private val devicePolicyManagerHelper: dagger.Lazy<DevicePolicyManagerHelper>,
    private val screenOffReceiverServiceManager: dagger.Lazy<ScreenOffReceiverServiceManager>
) : UserPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override val screenTimeouts = ScreenTimeoutRepository.screenTimeouts
    override val specialScreenTimeouts = ScreenTimeoutRepository.specialScreenTimeouts

    override suspend fun getKeepOnIsActive(): Boolean = getKeepOnIsActiveFlow().first()

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

    private suspend fun setDefaultToMaxAllowedValue(): ScreenTimeout {
        return withContext(ioDispatcher) {
            val maxAllowedScreenTimeout = getMaxAllowedScreenTimeout()
            val suitableTimeout = screenTimeouts.lastOrNull { it.value <= maxAllowedScreenTimeout }

            suitableTimeout?.also {
                setDefaultScreenTimeout(it)
            } ?: error("No suitable screen timeout found within the allowed maximum.")
            suitableTimeout
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout> =
        withContext(ioDispatcher) {
            val defaultValue = -1
            preferenceDataStoreHelper.getPreference(
                DEFAULT_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
                .transformLatest { storedValue ->
                    val currentTimeoutValue = storedValue
                    var currentScreenTimeout = ScreenTimeout(currentTimeoutValue)

                    if (currentTimeoutValue == defaultValue) {
                        val initializedTimeout = initDefaultScreenTimeout(
                            systemScreenTimeoutController.get()
                        )
                        currentScreenTimeout = initializedTimeout
                        emit(currentScreenTimeout)
                    } else if (!devicePolicyManagerHelper.get().isValidTimeout(currentScreenTimeout)) {
                        val maxAllowedTimeout = setDefaultToMaxAllowedValue()
                        emit(maxAllowedTimeout)
                    } else {
                        emit(currentScreenTimeout)
                    }
                }
                .distinctUntilChanged()
        }

    override suspend fun getDefaultScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val defaultValue = -1
            val persistedTimeoutValue = preferenceDataStoreHelper.getLastPreference(
                DEFAULT_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )

            var defaultScreenTimeout = if (persistedTimeoutValue == defaultValue) {
                initDefaultScreenTimeout(systemScreenTimeoutController.get())
            } else {
                ScreenTimeout(persistedTimeoutValue)
            }

            if (!devicePolicyManagerHelper.get().isValidTimeout(defaultScreenTimeout)) {
                defaultScreenTimeout = setDefaultToMaxAllowedValue()
            }

            defaultScreenTimeout
        }

    override suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout, checkService: Boolean) =
        withContext(ioDispatcher) {
            if (checkService && timeout == getCurrentScreenTimeout()) {
                screenOffReceiverServiceManager.get().stopService()
            }
            preferenceDataStoreHelper.putPreference(
                DEFAULT_SCREEN_TIMEOUT,
                timeout.value,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> =
        withContext(ioDispatcher) {
            val defaultValue = systemScreenTimeoutController.get().getSystemScreenTimeout()
            preferenceDataStoreHelper.getPreference(
                CURRENT_SCREEN_TIMEOUT,
                defaultValue.value,
                DataStoreSourceType.DATA_SOURCE
            )
                .transformLatest { emit(ScreenTimeout(it)) }
                .distinctUntilChanged()
        }

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val defaultValue = systemScreenTimeoutController.get().getSystemScreenTimeout()

            ScreenTimeout(
                preferenceDataStoreHelper.getLastPreference(
                    CURRENT_SCREEN_TIMEOUT,
                    defaultValue.value,
                    DataStoreSourceType.DATA_SOURCE
                )
            )
        }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                SELECTED_SCREEN_TIMEOUT,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { strList ->
                    // Manage migration from the older icon style model
                    val screenTimeoutList = if (strList.isNullOrEmpty()) {
                        DataMigrationHelper.getDefaultSelectedScreenTimeoutOrMigrateFromOld(
                            this@UserPreferencesRepositoryImpl
                        )
                    } else {
                        Json.decodeFromString<List<ScreenTimeout>>(strList)
                    }
                    emit(devicePolicyManagerHelper.get().removeNotAllowedScreenTimeout(screenTimeoutList))
                }
                .distinctUntilChanged()
        }

    override suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout> =
        withContext(ioDispatcher) {
            val strList = preferenceDataStoreHelper.getLastPreference(
                SELECTED_SCREEN_TIMEOUT,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            // Manage migration from the older icon style model
            devicePolicyManagerHelper.get().removeNotAllowedScreenTimeout(
                if (strList.isNullOrEmpty()) {
                    DataMigrationHelper.getDefaultSelectedScreenTimeoutOrMigrateFromOld(
                        this@UserPreferencesRepositoryImpl
                    )
                } else {
                    Json.decodeFromString<List<ScreenTimeout>>(strList)
                }
            )
        }

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                SELECTED_SCREEN_TIMEOUT,
                Json.encodeToString(selectedTimeouts),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { resetWhenScreenOff ->
                    // Manage migration from the older icon style model
                    emit(
                        resetWhenScreenOff ?: DataMigrationHelper.getResetTimeoutWhenScreenOffOrMigrateFromOld(this@UserPreferencesRepositoryImpl)
                    )
                }
                .distinctUntilChanged()
        }

    override suspend fun getResetTimeoutWhenScreenOff(): Boolean =
        withContext(ioDispatcher) {
            val resetWhenScreenOff = preferenceDataStoreHelper.getLastPreference(
                RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            // Manage migration from the older icon style model
            resetWhenScreenOff ?: DataMigrationHelper.getResetTimeoutWhenScreenOffOrMigrateFromOld(this@UserPreferencesRepositoryImpl)
        }

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
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getLastPreference(
                OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldResetTimeoutWhenScreenOff() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { timeoutIconStyleJson ->
                    // Manage migration from the older reset screen timeout value
                    emit(
                        if (timeoutIconStyleJson.isNullOrEmpty()) {
                            DataMigrationHelper.getDefaultTimeoutIconStyleOrMigrateFromOld(
                                this@UserPreferencesRepositoryImpl
                            )
                        } else {
                            Json.decodeFromString<TimeoutIconStyle>(timeoutIconStyleJson)
                        }
                    )
                }
                .distinctUntilChanged()
        }

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle =
        withContext(ioDispatcher) {
            val timeoutIconStyleJson = preferenceDataStoreHelper.getLastPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            // Manage migration from the older icon style model
            if (timeoutIconStyleJson.isNullOrEmpty()) {
                DataMigrationHelper.getDefaultTimeoutIconStyleOrMigrateFromOld(this@UserPreferencesRepositoryImpl)
            } else {
                Json.decodeFromString<TimeoutIconStyle>(timeoutIconStyleJson)
            }
        }

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                TIMEOUT_ICON_STYLE,
                Json.encodeToString(timeoutIconStyle),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle? =
        withContext(ioDispatcher) {
            val oldValue = preferenceDataStoreHelper.getLastPreference(
                OLD_TIMEOUT_ICON_STYLE,
                "",
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )

            if (oldValue.isNotEmpty()) {
                Json.decodeFromString<OldTimeoutIconStyle>(oldValue)
            } else {
                null
            }
        }

    override suspend fun removeOldTimeoutIconStyle() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getQSTileAddedFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getPreference(
                QSTILE_ADDED,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                QSTILE_ADDED,
                isAdded,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> =
        withContext(ioDispatcher) {
            val defaultValue = -1
            preferenceDataStoreHelper.getPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
                .transformLatest { emit(ScreenTimeout(it)) }
                .distinctUntilChanged()
        }

    private suspend fun getPreviousScreenTimeout(): ScreenTimeout {
        val defaultValue = -1
        return ScreenTimeout(
            preferenceDataStoreHelper.getLastPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
        )
    }

    private suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                timeout.value,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override suspend fun setNextSelectedSystemScreenTimeout(requestUpdateQSTile: suspend () -> Unit) =
        withContext(ioDispatcher) {
            val nextTimeoutValue = getNextSelectedScreenTimeout()
            val defaultTimeout = getDefaultScreenTimeout()
            val currentTimeout = getCurrentScreenTimeout()

            if (!devicePolicyManagerHelper.get().isValidTimeout(nextTimeoutValue)) {
                return@withContext
            }

            setSystemScreenTimeout(nextTimeoutValue) { requestUpdateQSTile() }

            updateDefaultScreenTimeoutIfNoResetTimeout(
                currentTimeout,
                defaultTimeout,
                nextTimeoutValue
            )
        }

    override suspend fun setNewSystemScreenTimeout(newTimeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean, requestUpdateQSTile: () -> Unit) =
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

            setSystemScreenTimeout(timeout, forceUpdatePreviousTimeout) { requestUpdateQSTile() }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                DISMISSED_TIPS,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { dismissedTipsStr ->
                    // Manage migration from the older value
                    emit(
                        if (dismissedTipsStr.isNullOrEmpty()) {
                            DataMigrationHelper.getDefaultDismissedTipsListOrMigrateFromOld(
                                this@UserPreferencesRepositoryImpl
                            )
                        } else {
                            Json.decodeFromString<List<DismissedTips>>(dismissedTipsStr)
                        }
                    )
                }
                .distinctUntilChanged()
        }

    private suspend fun getDismissedTips(): List<DismissedTips> =
        withContext(ioDispatcher) {
            val dismissedTipsStr = preferenceDataStoreHelper.getLastPreference(
                DISMISSED_TIPS,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            // Manage migration from the older value
            if (dismissedTipsStr.isNullOrEmpty()) {
                DataMigrationHelper.getDefaultDismissedTipsListOrMigrateFromOld(this@UserPreferencesRepositoryImpl)
            } else {
                Json.decodeFromString<List<DismissedTips>>(dismissedTipsStr)
            }
        }

    private suspend fun setDismissedTips(dismissedTips: List<DismissedTips>) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                DISMISSED_TIPS,
                Json.encodeToString<List<DismissedTips>>(dismissedTips),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun setDismissedTip(dismissedTips: DismissedTips) =
        withContext(ioDispatcher) {
            val dismissedTipList = getDismissedTips().toMutableList()
            dismissedTipList.add(dismissedTips)
            setDismissedTips(dismissedTipList)
        }

    override suspend fun getOldSelectedScreenTimeouts(): String =
        withContext(ioDispatcher) {
            val defaultValue = ""
            preferenceDataStoreHelper.getLastPreference(
                OLD_SELECTED_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldSelectedScreenTimeouts() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_SELECTED_SCREEN_TIMEOUT,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldAppReviewAsked(): Boolean =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getLastPreference(
                OLD_APP_REVIEW_ASKED,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldAppReviewAsked() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_APP_REVIEW_ASKED,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldSkipIntro(): Boolean =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getLastPreference(
                OLD_SKIP_INTRO,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldSkipIntro() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_SKIP_INTRO,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getIsFirstLaunchFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                IS_FIRST_LAUNCH,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { isFirstLaunch ->
                    // Manage migration from the older value
                    emit(
                        isFirstLaunch
                            ?: DataMigrationHelper.getDefaultIsFirstLaunchOrMigrateFromOld(this@UserPreferencesRepositoryImpl)
                    )
                }
                .distinctUntilChanged()
        }

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                IS_FIRST_LAUNCH,
                isFirstLaunch,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getAppLaunchCountFlow(): Flow<Long> =
        withContext(ioDispatcher) {
            val defaultValue = 0L
            preferenceDataStoreHelper.getPreference(
                APP_LAUNCH_COUNT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getAppLaunchCount(): Long =
        withContext(ioDispatcher) {
            val defaultValue = 0L
            preferenceDataStoreHelper.getLastPreference(
                APP_LAUNCH_COUNT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun setAppLaunchCount(appLaunchCount: Long) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                APP_LAUNCH_COUNT,
                appLaunchCount,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun resetSystemScreenTimeoutToDefault(requestUpdateQSTile: () -> Unit) =
        withContext(ioDispatcher) {
            val defaultTimeout = getDefaultScreenTimeout()
            setSystemScreenTimeout(
                defaultTimeout
            ) { requestUpdateQSTile() }
            screenOffReceiverServiceManager.get().stopService()
        }

    private suspend fun initDefaultScreenTimeout(systemScreenTimeoutController: SystemScreenTimeoutController): ScreenTimeout =
        withContext(ioDispatcher) {
            val initialValue = systemScreenTimeoutController.getSystemScreenTimeout()
            setDefaultScreenTimeout(initialValue)
            initialValue
        }

    private suspend fun getNextSelectedScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val screenTimeouts = getSelectedScreenTimeouts().toMutableSet()
            val currentTimeout = getCurrentScreenTimeout()
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

    override suspend fun getLastRunVersionCode(): Long {
        return preferenceDataStoreHelper.getLastPreference(
            LAST_RUN_VERSION_CODE,
            0,
            DataStoreSourceType.DATA_SOURCE
        )
    }
    override suspend fun setLastRunVersionCode(versionCode: Long) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                LAST_RUN_VERSION_CODE,
                versionCode,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override fun getMaxAllowedScreenTimeout() = devicePolicyManagerHelper.get().getMaxAllowedScreenTimeout()

    private suspend fun setSystemScreenTimeout(
        timeout: ScreenTimeout,
        forceUpdatePreviousTimeout: Boolean = false,
        invokeUpdateQSTile: suspend () -> Unit,
    ) = withContext(ioDispatcher) {
        // Check if timeout value is allowed
        if (!devicePolicyManagerHelper.get().isValidTimeout(timeout)) {
            return@withContext
        }

        // Update flow directly to prevent lag in UI (value will be override by the worker)
        setCurrentScreenTimeout(timeout, forceUpdatePreviousTimeout)
        invokeUpdateQSTile()

        // Set the desired timeout and update the system screen timeout
        DesiredScreenTimeoutController.setDesiredScreenTimeout(timeout, systemScreenTimeoutController.get())
    }
}
