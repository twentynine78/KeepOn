package fr.twentynine.keepon.core.permission

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import javax.inject.Inject

class PermissionStateGatewayImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PermissionStateGateway {

    private val powerManager by lazy { context.getSystemService(PowerManager::class.java) }

    override fun canWriteSystemSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    override fun isBatteryNotOptimized(): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun areRequiredPermissionsGranted(): Boolean {
        return canWriteSystemSettings() && isBatteryNotOptimized()
    }
}
