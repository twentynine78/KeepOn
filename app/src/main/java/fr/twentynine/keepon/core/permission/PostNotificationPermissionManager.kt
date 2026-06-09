package fr.twentynine.keepon.core.permission

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ActivityContext
import fr.twentynine.keepon.R
import javax.inject.Inject

/**
 * Requests the runtime POST_NOTIFICATIONS permission (Android 13+) through a caller-supplied
 * launcher. Also owns the screen-monitor notification channel definition, since the channel must
 * exist before the foreground service posts its notification.
 */
interface PostNotificationPermissionManager {
    fun requestPostNotificationPermission(
        requestPostNotificationPermissionLauncher: ActivityResultLauncher<String>
    )

    companion object {
        const val NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID = "keepon_screen_monitor"
        const val NOTIFICATION_GROUP_KEY = "keepon_notification"

        /** Creates (idempotently) the minimal-importance, badge-less channel for the screen-monitor service. */
        fun createNotificationChannel(context: Context) {
            val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_SCREEN_MONITOR_ID,
                context.getString(R.string.notification_channel_screen_monitor_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
                enableLights(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

/** Activity-scoped impl that ensures the channel exists on creation and launches the 13+ prompt. */
class PostNotificationPermissionManagerImpl @Inject constructor(
    @param:ActivityContext private val context: Context
) : PostNotificationPermissionManager {

    init {
        // Create notification channel if needed
        PostNotificationPermissionManager.createNotificationChannel(context)
    }

    override fun requestPostNotificationPermission(
        requestPostNotificationPermissionLauncher: ActivityResultLauncher<String>,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}
