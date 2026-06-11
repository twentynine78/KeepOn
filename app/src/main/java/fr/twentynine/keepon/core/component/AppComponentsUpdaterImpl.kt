package fr.twentynine.keepon.core.component

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.WidgetUpdater
import javax.inject.Inject

/** Refreshes the app's external surfaces after a state change — currently the home-screen widget. */
class AppComponentsUpdaterImpl @Inject constructor(
    private val widgetUpdater: WidgetUpdater,
) : AppComponentsUpdater {
    override suspend fun requestUpdate() {
        widgetUpdater.requestUpdateWidget()
    }
}
