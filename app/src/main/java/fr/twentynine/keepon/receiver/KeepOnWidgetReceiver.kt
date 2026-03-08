package fr.twentynine.keepon.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import fr.twentynine.keepon.widget.KeepOnWidget

class KeepOnWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KeepOnWidget()
}
