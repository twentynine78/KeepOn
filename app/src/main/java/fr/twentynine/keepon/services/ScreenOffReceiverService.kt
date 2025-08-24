package fr.twentynine.keepon.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.receiver.ScreenOffReceiver
import fr.twentynine.keepon.tasker.FireReceiver
import fr.twentynine.keepon.tasker.PluginBundleManager
import fr.twentynine.keepon.tasker.TaskerIntent
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.PostNotificationPermissionManager.Companion.NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID

class ScreenOffReceiverService : LifecycleService() {

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
        Intent(this, FireReceiver::class.java)
            .setAction(TaskerIntent.ACTION_FIRE_SETTING)
            .putExtra(
                TaskerIntent.EXTRA_BUNDLE,
                Bundle().apply {
                    putInt(
                        PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE,
                        SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value
                    )
                }
            )
    }
    private val pendingStopKeepOnIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(this, 0, stopKeepOnIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onCreate() {
        super.onCreate()

        registerScreenOffReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentAction = intent?.action

        if (intentAction != null) {
            when (intentAction) {
                ScreenOffReceiverServiceManager.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    stopSelf()
                }
            }
        } else {
            try {
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

                ScreenOffReceiverServiceManager.setIsRunning(true)
            } catch (_: Exception) {}
        }

        return super.onStartCommand(intent, flags, START_STICKY)
    }

    override fun onDestroy() {
        ScreenOffReceiverServiceManager.setIsRunning(false)

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
                ContextCompat.RECEIVER_EXPORTED
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
    }
}
