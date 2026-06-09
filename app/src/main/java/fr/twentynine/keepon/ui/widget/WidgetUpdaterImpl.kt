package fr.twentynine.keepon.ui.widget

import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.WidgetUpdater
import fr.twentynine.keepon.receiver.KeepOnWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implements [WidgetUpdater] over Glance: `requestUpdateWidget` recomposes all widget instances, and
 * `requestUpdateWidgetPreview` refreshes the launcher widget-picker preview (Android 15+, a no-op on
 * older versions).
 */
class WidgetUpdaterImpl @Inject constructor(@param:ApplicationContext private val context: Context) : WidgetUpdater {
    override suspend fun requestUpdateWidget() {
        withContext(Dispatchers.IO) {
            KeepOnWidget().updateAll(context)
        }
    }

    override suspend fun requestUpdateWidgetPreview(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return withContext(Dispatchers.IO) {
                val setPreviewResult =
                    GlanceAppWidgetManager(context).setWidgetPreviews<KeepOnWidgetReceiver>()
                return@withContext setPreviewResult == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
            }
        } else {
            return true
        }
    }
}
