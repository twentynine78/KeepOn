package fr.twentynine.keepon.core.permission

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.NotificationChannelManager
import javax.inject.Inject

class NotificationChannelManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NotificationChannelManager {

    override fun removeLegacyKeepOnServiceChannel() {
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_OLD_KEEPON_SERVICE)
    }

    private companion object {
        const val NOTIFICATION_CHANNEL_OLD_KEEPON_SERVICE = "keepon_services"
    }
}
