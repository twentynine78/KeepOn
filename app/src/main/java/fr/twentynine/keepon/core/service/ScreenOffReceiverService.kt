package fr.twentynine.keepon.core.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.core.receiver.ScreenOffReceiver
import fr.twentynine.keepon.core.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.core.permission.PostNotificationPermissionManager.Companion.NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID
import fr.twentynine.keepon.domain.usecase.timeout.ResetSystemScreenTimeoutUseCase
import fr.twentynine.keepon.di.qualifier.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenOffReceiverService : LifecycleService() {

    @Inject
    lateinit var resetSystemScreenTimeoutUseCase: ResetSystemScreenTimeoutUseCase

    @Inject
    lateinit var screenOffServiceState: ScreenOffServiceState

    // Run on the app scope so the reset (which stops this service mid-way) completes
    // the system write even after the service is destroyed.
    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    private val screenOffReceiver by lazy { ScreenOffReceiver() }

    private val launchKeepOnIntent: Intent by lazy {
        Intent(this, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
    }
    private val pendingLaunchKeepOnIntent: PendingIntent by lazy {
        PendingIntent.getActivity(this, 0, launchKeepOnIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    private val stopKeepOnIntent: Intent by lazy {
        Intent(this, ScreenOffReceiverService::class.java)
            .setAction(ACTION_STOP_KEEPON)
    }
    private val pendingStopKeepOnIntent: PendingIntent by lazy {
        PendingIntent.getService(this, 0, stopKeepOnIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onCreate() {
        super.onCreate()

        PostNotificationPermissionManager.createNotificationChannel(this)

        registerScreenOffReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ScreenOffReceiverServiceManagerImpl.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP_KEEPON -> {
                applicationScope.launch {
                    try {
                        resetSystemScreenTimeoutUseCase()
                    } catch (_: Exception) {
                        // The write-settings permission may have been revoked; stop anyway.
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }
        }

        return try {
            // Start foreground service
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
            screenOffServiceState.isRunning = true
            START_STICKY
        } catch (_: Exception) {
            // If startForeground fails, we must stop the service immediately.
            // This prevents RemoteServiceException (crash) triggered by the system
            // if a service promised as "foreground" doesn't call startForeground() within 5s.
            screenOffServiceState.isRunning = false
            stopSelf()
            START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        screenOffServiceState.isRunning = false

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)

        unregisterScreenOffReceiver()

        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID)
            .setOngoing(true)
            .setContentTitle(getString(R.string.notification_screen_monitor_title))
            .setContentText(getString(R.string.notification_screen_monitor_text))
            .setSmallIcon(R.drawable.ic_keepon)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .addAction(
                R.drawable.ic_launch,
                getString(R.string.notification_screen_monitor_action_open_keepon),
                pendingLaunchKeepOnIntent
            )
            .addAction(
                R.drawable.ic_close,
                getString(R.string.notification_screen_monitor_action_stop_keepon),
                pendingStopKeepOnIntent
            )
            .setGroup(PostNotificationPermissionManager.NOTIFICATION_GROUP_KEY)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun registerScreenOffReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)

        try {
            ContextCompat.registerReceiver(
                this,
                screenOffReceiver,
                intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: IllegalArgumentException) {}
    }

    private fun unregisterScreenOffReceiver() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: IllegalArgumentException) {}
    }

    companion object {
        private const val NOTIFICATION_ID = 1111
        const val ACTION_STOP_KEEPON = "ACTION_STOP_KEEPON"
    }
}
