package fr.twentynine.keepon.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.annotation.ServiceScope
import toothpick.InjectConstructor

@ServiceScope
@InjectConstructor
class ServiceUtils(private val service: Service) {

    private val hideIntent: Intent by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, service.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        } else {
            val uri = Uri.fromParts("package", service.packageName, null)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
    }
    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(service, 0, hideIntent, 0)
    }

    fun buildNotification(contentText: String): Notification {
        createNotificationChannel()
        // Return notification
        return NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentText)
            .setContentText(service.getString(R.string.notification_hide))
            .setSmallIcon(R.mipmap.ic_qs_keepon)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = service.getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            chan.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(chan)
        }
    }

    companion object {
        internal const val NOTIFICATION_CHANNEL_ID = "keepon_services"
    }
}
