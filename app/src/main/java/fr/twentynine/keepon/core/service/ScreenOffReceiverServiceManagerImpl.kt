package fr.twentynine.keepon.core.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.DebugTracer
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
    private val tracer: DebugTracer,
) : ScreenOffReceiverServiceManager {
    override suspend fun startService() {
        if (screenOffServiceState.isRunning) {
            tracer.trace(TAG) { "start skipped: already running" }
            return
        }
        if (!permissionStateGateway.areRequiredPermissionsGranted()) {
            tracer.trace(TAG) { "start refused: required permissions missing" }
            userNotifier.notifyMissingPermission()
            return
        }
        try {
            tracer.trace(TAG) { "starting the screen-off foreground service" }
            ContextCompat.startForegroundService(
                context,
                Intent(context.applicationContext, ScreenOffReceiverService::class.java)
            )
        } catch (e: Exception) {
            // Permissions are granted, so this is a start failure (e.g. the Android 12+
            // foreground-service background-start restriction), not a missing permission.
            tracer.trace(TAG) { "start failed: $e" }
            userNotifier.notifyScreenOffServiceError()
        }
    }

    override suspend fun stopService() {
        if (!screenOffServiceState.isRunning) {
            return
        }
        tracer.trace(TAG) { "stopping the screen-off foreground service" }
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
        tracer.trace(TAG) { "restarting (re-issuing foreground start to re-post the notification)" }
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
        private const val TAG = "Service"
    }
}
