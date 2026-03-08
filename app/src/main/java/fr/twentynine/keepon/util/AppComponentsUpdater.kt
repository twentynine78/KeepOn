package fr.twentynine.keepon.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AppComponentsUpdater {
    suspend fun requestUpdate(): Job
}

class AppComponentsUpdaterImpl @Inject constructor(
    private val qsTileUpdater: QSTileUpdater,
    private val widgetUpdater: WidgetUpdater,
) : AppComponentsUpdater {
    override suspend fun requestUpdate() = coroutineScope {
        launch { qsTileUpdater.requestUpdate() }
        launch { widgetUpdater.requestUpdateWidget() }
    }
}
