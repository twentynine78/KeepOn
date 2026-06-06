package fr.twentynine.keepon.core.system

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.AppVersionProvider
import javax.inject.Inject

class AppVersionProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppVersionProvider {

    override fun getCurrentVersionCode(): Long {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        } catch (_: PackageManager.NameNotFoundException) {
            -1
        }
    }
}
