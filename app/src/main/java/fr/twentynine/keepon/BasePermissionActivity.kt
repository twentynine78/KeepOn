package fr.twentynine.keepon

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.core.permission.BatteryOptimizationManager
import fr.twentynine.keepon.core.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.core.permission.SystemSettingPermissionManager
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base activity factoring out the permission wiring: the injected permission managers,
 * the POST_NOTIFICATIONS launcher, the request actions and the on-resume state refresh.
 *
 * Subclasses map their own UI events to [requestWriteSystemSettingPermission],
 * [requestDisableBatteryOptimization], [requestPostNotificationPermission] and
 * [checkNeededPermissions].
 */
@AndroidEntryPoint
abstract class BasePermissionActivity : ComponentActivity() {

    @Inject
    lateinit var systemSettingPermissionManager: SystemSettingPermissionManager

    @Inject
    lateinit var postNotificationPermissionManager: PostNotificationPermissionManager

    @Inject
    lateinit var batteryOptimizationManager: BatteryOptimizationManager

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    @Inject
    lateinit var permissionStateGateway: PermissionStateGateway

    private val requestPostNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionStateGateway.setPostNotificationGranted(isGranted)
            if (isGranted) {
                lifecycleScope.launch { screenOffReceiverServiceManager.restartService() }
            }
        }

    protected fun requestWriteSystemSettingPermission() =
        systemSettingPermissionManager.requestWriteSystemSettingsPermission()

    protected fun requestDisableBatteryOptimization() =
        batteryOptimizationManager.requestDisableBatteryOptimization()

    protected fun requestPostNotificationPermission() =
        postNotificationPermissionManager.requestPostNotificationPermission(requestPostNotificationLauncher)

    protected fun checkNeededPermissions() {
        permissionStateGateway.refreshWriteSystemSettings()
        permissionStateGateway.refreshBatteryOptimization()
    }

    override fun onResume() {
        super.onResume()

        permissionStateGateway.refreshWriteSystemSettings()
        permissionStateGateway.refreshBatteryOptimization()
        permissionStateGateway.refreshPostNotification()
    }
}
