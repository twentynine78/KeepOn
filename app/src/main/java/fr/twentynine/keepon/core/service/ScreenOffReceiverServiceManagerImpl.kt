package fr.twentynine.keepon.core.service

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScreenOffReceiverServiceManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionStateGateway: PermissionStateGateway,
    private val screenOffServiceState: ScreenOffServiceState,
) : ScreenOffReceiverServiceManager {
    override suspend fun startService() {
        if (screenOffServiceState.isRunning) {
            return
        }
        if (!permissionStateGateway.areRequiredPermissionsGranted()) {
            showToast(R.string.toast_missing_permission)
            return
        }
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context.applicationContext, ScreenOffReceiverService::class.java)
            )
        } catch (_: Exception) {
            // Permissions are granted, so this is a start failure (e.g. the Android 12+
            // foreground-service background-start restriction), not a missing permission.
            showToast(R.string.toast_screen_off_service_error)
        }
    }

    override suspend fun stopService() {
        if (!screenOffServiceState.isRunning) {
            return
        }
        try {
            context.startService(
                Intent(
                    context.applicationContext,
                    ScreenOffReceiverService::class.java
                ).also {
                    it.action = ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
                }
            )
        } catch (_: Exception) {
            showToast(R.string.toast_screen_off_service_error)
        }
    }

    override suspend fun restartService() {
        if (!screenOffServiceState.isRunning) {
            return
        }
        // The service is already running; re-issue the foreground start so onStartCommand re-posts
        // the notification (e.g. once POST_NOTIFICATIONS was granted) without a stop → delay → start
        // cycle that would briefly tear the service down.
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context.applicationContext, ScreenOffReceiverService::class.java)
            )
        } catch (_: Exception) {
            showToast(R.string.toast_screen_off_service_error)
        }
    }

    private suspend fun showToast(@StringRes messageId: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context.applicationContext,
                context.getString(messageId),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE"
    }
}
