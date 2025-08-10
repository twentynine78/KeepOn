package fr.twentynine.keepon.util

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.services.KeepOnTileService
import javax.inject.Inject

interface QSTileUpdater {
    fun requestUpdate()
}

class QSTileUpdaterImpl @Inject constructor(@param:ApplicationContext private val context: Context) : QSTileUpdater {

    override fun requestUpdate() {
        TileService.requestListeningState(
            context.applicationContext,
            ComponentName(context.applicationContext, KeepOnTileService::class.java)
        )
    }
}
