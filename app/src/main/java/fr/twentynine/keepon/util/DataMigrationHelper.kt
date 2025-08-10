package fr.twentynine.keepon.util

import fr.twentynine.keepon.data.local.TipsInfo
import fr.twentynine.keepon.data.model.DismissedTips
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataMigrationHelper {
    private val ioDispatcher = Dispatchers.IO

    suspend fun getDefaultSelectedScreenTimeoutOrMigrateFromOld(
        userPreferencesRepository: UserPreferencesRepository
    ): List<ScreenTimeout> =
        withContext(ioDispatcher) {
            val oldSelectedScreenTimeout = userPreferencesRepository.getOldSelectedScreenTimeouts()
            return@withContext if (oldSelectedScreenTimeout.isNotEmpty()) {
                // Migrate old config
                val newList = intListFromStr(oldSelectedScreenTimeout).map { timeoutValue ->
                    ScreenTimeout(timeoutValue)
                }
                userPreferencesRepository.setSelectedScreenTimeouts(newList)
                userPreferencesRepository.removeOldSelectedScreenTimeouts()

                newList
            } else {
                emptyList<ScreenTimeout>()
            }
        }

    suspend fun getDefaultTimeoutIconStyleOrMigrateFromOld(
        userPreferencesRepository: UserPreferencesRepository
    ): TimeoutIconStyle =
        withContext(ioDispatcher) {
            val oldTimeoutIconStyle = userPreferencesRepository.getOldTimeoutIconStyle()
            return@withContext if (oldTimeoutIconStyle != null) {
                // Migrate old config
                val newTimeoutIconStyle = oldTimeoutIconStyle.toTimeoutIconStyle
                userPreferencesRepository.setTimeoutIconStyle(newTimeoutIconStyle)
                userPreferencesRepository.removeOldTimeoutIconStyle()

                newTimeoutIconStyle
            } else {
                TimeoutIconStyle()
            }
        }

    suspend fun getDefaultDismissedTipsListOrMigrateFromOld(
        userPreferencesRepository: UserPreferencesRepository
    ): List<DismissedTips> =
        withContext(ioDispatcher) {
            val oldAppReviewAsked = userPreferencesRepository.getOldAppReviewAsked()
            return@withContext if (oldAppReviewAsked) {
                // Migrate old config
                val rateAppTip = DismissedTips(TipsInfo.RateApp.id)
                userPreferencesRepository.setDismissedTip(rateAppTip)
                userPreferencesRepository.removeOldAppReviewAsked()

                listOf(rateAppTip)
            } else {
                emptyList()
            }
        }

    suspend fun getDefaultIsFirstLaunchOrMigrateFromOld(
        userPreferencesRepository: UserPreferencesRepository
    ): Boolean =
        withContext(ioDispatcher) {
            // Migrate old config
            val oldSkipIntro = userPreferencesRepository.getOldSkipIntro()

            if (oldSkipIntro) {
                userPreferencesRepository.setIsFirstLaunch(false)
                userPreferencesRepository.removeOldSkipIntro()
            }

            return@withContext !oldSkipIntro
        }

    suspend fun getResetTimeoutWhenScreenOffOrMigrateFromOld(
        userPreferencesRepository: UserPreferencesRepository
    ): Boolean =
        withContext(ioDispatcher) {
            // Migrate old config
            val oldResetTimeoutWhenScreenOff = userPreferencesRepository.getOldResetTimeoutWhenScreenOff()

            return@withContext if (oldResetTimeoutWhenScreenOff != null) {
                userPreferencesRepository.removeOldResetTimeoutWhenScreenOff()
                userPreferencesRepository.setResetTimeoutWhenScreenOff(!oldResetTimeoutWhenScreenOff)

                !oldResetTimeoutWhenScreenOff
            } else {
                true
            }
        }

    private fun intListFromStr(stringIntList: String?): List<Int> {
        // Retrieve old data format
        val resultList: ArrayList<Int> = ArrayList()
        val tempList = stringIntList?.split("|")
        if (tempList != null) {
            for (string: String in tempList) {
                if (string.isNotEmpty()) {
                    resultList.add(string.toInt())
                }
            }
        }
        return resultList
    }
}
