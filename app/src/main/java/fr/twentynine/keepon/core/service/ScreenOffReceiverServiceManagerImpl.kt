package fr.twentynine.keepon.core.service

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

class ScreenOffReceiverServiceManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionStateGateway: PermissionStateGateway,
) : ScreenOffReceiverServiceManager {
    override suspend fun startService() {
        if (!getIsRunning()) {
            if (permissionStateGateway.areRequiredPermissionsGranted()) {
                try {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    )
                } catch (_: Exception) {
                    showMissingPermissionToast()
                }
            } else {
                showMissingPermissionToast()
            }
        }
    }

    override suspend fun stopService() {
        if (getIsRunning()) {
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
                showMissingPermissionToast()
            }
        }
    }

    override suspend fun restartService() {
        if (!getIsRunning()) {
            return
        }
        stopService()
        delay(500.milliseconds)
        startService()
    }

    private suspend fun showMissingPermissionToast() {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context.applicationContext,
                context.getString(R.string.toast_missing_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE"

        @Volatile
        private var isRunning: Boolean = false

        fun getIsRunning(): Boolean {
            synchronized(this) {
                return isRunning
            }
        }
        fun setIsRunning(running: Boolean) {
            synchronized(this) {
                isRunning = running
            }
        }
    }
}
