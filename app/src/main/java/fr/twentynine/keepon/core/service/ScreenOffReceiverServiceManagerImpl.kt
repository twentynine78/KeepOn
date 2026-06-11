package fr.twentynine.keepon.core.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.gateway.UserNotifier
import javax.inject.Inject

/**
 * Starts/stops/restarts the [ScreenOffReceiverService], guarding the start on the required permissions
 * and the tracked running state (so it isn't started twice or without permission), and notifying the
 * outcome where relevant.
 */
class ScreenOffReceiverServiceManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionStateGateway: PermissionStateGateway,
    private val screenOffServiceState: ScreenOffServiceState,
    private val userNotifier: UserNotifier,
) : ScreenOffReceiverServiceManager {
    override suspend fun startService() {
        if (screenOffServiceState.isRunning) {
            return
        }
        if (!permissionStateGateway.areRequiredPermissionsGranted()) {
            userNotifier.notifyMissingPermission()
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
            userNotifier.notifyScreenOffServiceError()
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
            userNotifier.notifyScreenOffServiceError()
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
            userNotifier.notifyScreenOffServiceError()
        }
    }

    companion object {
        const val ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE"
    }
}
