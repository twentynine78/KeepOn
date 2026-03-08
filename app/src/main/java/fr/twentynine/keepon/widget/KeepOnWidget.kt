package fr.twentynine.keepon.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import fr.twentynine.keepon.data.repo.WidgetRepository
import fr.twentynine.keepon.ui.widget.KeepOnWidgetPreview
import fr.twentynine.keepon.ui.widget.KeepOnWidgetView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KeepOnWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Responsive(WIDGET_SIZES)

    override val previewSizeMode: PreviewSizeMode
        get() = SizeMode.Responsive(WIDGET_SIZES)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        withContext(Dispatchers.IO) {
            WidgetRepository.updateWidgetUIState(context)
        }

        provideContent {
            val widgetUIState by WidgetRepository.currentWidgetUIState.collectAsState()

            KeepOnWidgetView(widgetUIState)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val widgetUIState = withContext(Dispatchers.IO) {
            WidgetRepository.getWidgetPreviewUIState(context)
        }

        provideContent {
            KeepOnWidgetPreview(widgetUIState)
        }
    }

    companion object {
        val SMALL_SQUARE = DpSize(60.dp, 60.dp)
        private val MEDIUM_SQUARE = DpSize(90.dp, 90.dp)
        private val LARGE_SQUARE = DpSize(120.dp, 120.dp)

        private val WIDGET_SIZES = setOf(
            SMALL_SQUARE,
            MEDIUM_SQUARE,
            LARGE_SQUARE,
        )
    }
}
