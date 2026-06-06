package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.AppInfo

/**
 * Provides the app's display information (version, author, source URL).
 */
interface AppInfoProvider {
    fun getAppInfo(): AppInfo
}
