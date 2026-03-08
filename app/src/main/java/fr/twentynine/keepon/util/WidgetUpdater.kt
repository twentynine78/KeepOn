package fr.twentynine.keepon.util

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.data.repo.WidgetRepository
import fr.twentynine.keepon.receiver.KeepOnWidgetReceiver
import fr.twentynine.keepon.widget.KeepOnWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface WidgetUpdater {
    suspend fun requestUpdateWidget()
    suspend fun requestUpdateWidgetPreview(): Boolean
}

class WidgetUpdaterImpl @Inject constructor(@param:ApplicationContext private val context: Context) : WidgetUpdater {
    override suspend fun requestUpdateWidget() {
        withContext(Dispatchers.IO) {
            WidgetRepository.updateWidgetUIState(context)
            KeepOnWidget().updateAll(context)
        }
    }

    override suspend fun requestUpdateWidgetPreview(): Boolean {
        return withContext(Dispatchers.IO) {
            val setPreviewResult = GlanceAppWidgetManager(context).setWidgetPreviews<KeepOnWidgetReceiver>()
            return@withContext setPreviewResult == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
        }
    }
}
