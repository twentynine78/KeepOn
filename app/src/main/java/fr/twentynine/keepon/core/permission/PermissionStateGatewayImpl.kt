package fr.twentynine.keepon.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class PermissionStateGatewayImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PermissionStateGateway {

    private val powerManager by lazy { context.getSystemService(PowerManager::class.java) }

    private val _canWriteSystemSetting = MutableStateFlow(false)
    override val canWriteSystemSetting: Flow<Boolean> = _canWriteSystemSetting.asStateFlow()

    private val _batteryIsNotOptimized = MutableStateFlow(false)
    override val batteryIsNotOptimized: Flow<Boolean> = _batteryIsNotOptimized.asStateFlow()

    private val _canPostNotification = MutableStateFlow(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    override val canPostNotification: Flow<Boolean> = _canPostNotification.asStateFlow()

    override fun canWriteSystemSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    override fun isBatteryNotOptimized(): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun areRequiredPermissionsGranted(): Boolean {
        return canWriteSystemSettings() && isBatteryNotOptimized()
    }

    override fun refreshWriteSystemSettings() {
        _canWriteSystemSetting.update { canWriteSystemSettings() }
    }

    override fun refreshBatteryOptimization() {
        _batteryIsNotOptimized.update { isBatteryNotOptimized() }
    }

    override fun refreshPostNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _canPostNotification.update {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun setPostNotificationGranted(granted: Boolean) {
        _canPostNotification.update { granted }
    }
}
