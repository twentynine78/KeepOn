package fr.twentynine.keepon.ui.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.data.model.WidgetUIState

@Composable
fun KeepOnWidgetError(
    widgetUIState: WidgetUIState.Error,
    modifier: GlanceModifier = GlanceModifier,
) {
    val mainActivityIntent = Intent(LocalContext.current, MainActivity::class.java).apply {
        this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity(mainActivityIntent))
            .background(KeepOnWidgetColorScheme.colors.background)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = widgetUIState.error,
            style = TextStyle(
                KeepOnWidgetColorScheme.colors.tertiary
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
    }
}
