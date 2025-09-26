package fr.twentynine.keepon.services

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager.Companion.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager.Companion.getIsRunning
import fr.twentynine.keepon.util.RequiredPermissionsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

interface ScreenOffReceiverServiceManager {
    suspend fun startService()
    suspend fun stopService()
    suspend fun restartService()

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

class ScreenOffReceiverServiceManagerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : ScreenOffReceiverServiceManager {
    override suspend fun startService() {
        if (!getIsRunning()) {
            if (RequiredPermissionsManager.isPermissionsGranted(context)) {
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
                        it.action =
                            ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
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
        delay(Duration.ofMillis(500))
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
}
