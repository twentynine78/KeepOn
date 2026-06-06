package fr.twentynine.keepon.data.catalog

import android.content.Context
import fr.twentynine.keepon.domain.model.AppInfo

object AppInfoCatalog {

    fun getKeepOnAppInfo(context: Context): AppInfo {
        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        val appAuthor = "TwentyNine78"
        val codeSourceUrl = "https://github.com/twentynine78/KeepOn"

        return AppInfo(appVersion, appAuthor, codeSourceUrl)
    }
}
