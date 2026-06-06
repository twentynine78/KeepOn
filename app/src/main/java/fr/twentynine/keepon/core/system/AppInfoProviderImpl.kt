package fr.twentynine.keepon.core.system

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.AppInfoProvider
import fr.twentynine.keepon.domain.model.AppInfo
import javax.inject.Inject

class AppInfoProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppInfoProvider {

    override fun getAppInfo(): AppInfo {
        val appVersion = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"

        return AppInfo(
            version = appVersion,
            author = APP_AUTHOR,
            sourceCodeUrl = CODE_SOURCE_URL,
        )
    }

    private companion object {
        const val APP_AUTHOR = "TwentyNine78"
        const val CODE_SOURCE_URL = "https://github.com/twentynine78/KeepOn"
    }
}
