package fr.twentynine.keepon.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ActivityContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.util.PostNotificationPermissionManager.Companion.NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface PostNotificationPermissionManager {
    val canPostNotification: StateFlow<Boolean>

    fun updatePostNotificationPermission(canPostNotification: Boolean)
    fun checkPostNotificationPermission()
    fun requestPostNotificationPermission(
        requestPostNotificationPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    )

    companion object {
        const val NOTIFICATION_CHANNEL_OLD_KEEPON_SERVICE = "keepon_services"
        const val NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID = "keepon_screen_monitor"
        const val NOTIFICATION_GROUP_KEY = "keepon_notification"

        fun removeOldNotificationChannelKeepOnService(context: Context) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_OLD_KEEPON_SERVICE)
        }
    }
}

class PostNotificationPermissionManagerImpl @Inject constructor(
    @param:ActivityContext private val context: Context
) : PostNotificationPermissionManager {

    private val _canPostNotification = MutableStateFlow(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    override val canPostNotification = _canPostNotification.asStateFlow()

    private val notificationManager by lazy { context.getSystemService(NotificationManager::class.java) }

    private val notificationChannelKeepOnScreenMonitor = NotificationChannel(
        NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID,
        context.getString(
            R.string.notification_channel_screen_monitor_name
        ),
        NotificationManager.IMPORTANCE_MIN
    ).also {
        it.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
        it.enableLights(false)
        it.setShowBadge(false)
    }
    init {
        // Create notification channel if needed
        createNotificationChannelKeepOnScreenMonitor()
    }

    private fun createNotificationChannelKeepOnScreenMonitor() {
        notificationManager.createNotificationChannel(notificationChannelKeepOnScreenMonitor)
    }

    override fun updatePostNotificationPermission(canPostNotification: Boolean) {
        _canPostNotification.update { canPostNotification }
    }

    override fun checkPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            _canPostNotification.update {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun requestPostNotificationPermission(
        requestPostNotificationPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}
