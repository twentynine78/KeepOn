package fr.twentynine.keepon.data.repo

import android.content.Context
import fr.twentynine.keepon.data.model.AppInfo

class AppInfoRepository {

    fun getKeepOnAppInfo(context: Context): AppInfo {
        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        val appAuthor = "TwentyNine78"
        val codeSourceUrl = "https://github.com/twentynine78/KeepOn"

        return AppInfo(appVersion, appAuthor, codeSourceUrl)
    }
}
