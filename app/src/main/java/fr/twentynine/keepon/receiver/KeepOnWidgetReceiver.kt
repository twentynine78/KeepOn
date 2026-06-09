package fr.twentynine.keepon.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import fr.twentynine.keepon.ui.widget.KeepOnWidget

/** Manifest-declared receiver that binds the [KeepOnWidget] Glance widget to the home screen. */
class KeepOnWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KeepOnWidget()
}
