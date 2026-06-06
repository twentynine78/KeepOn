package fr.twentynine.keepon.core.component

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.QSTileUpdater
import fr.twentynine.keepon.domain.gateway.WidgetUpdater
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
