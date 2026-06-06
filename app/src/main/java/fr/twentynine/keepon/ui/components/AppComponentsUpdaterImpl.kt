package fr.twentynine.keepon.ui.components

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppComponentsUpdaterImpl @Inject constructor(
    private val qsTileUpdater: QSTileUpdater,
    private val widgetUpdater: WidgetUpdater,
) : AppComponentsUpdater {
    override suspend fun requestUpdate() = coroutineScope {
        launch { qsTileUpdater.requestUpdate() }
        launch { widgetUpdater.requestUpdateWidget() }
    }
}
