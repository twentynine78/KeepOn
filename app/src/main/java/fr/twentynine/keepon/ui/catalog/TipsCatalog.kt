package fr.twentynine.keepon.ui.catalog

/** The full set of [TipsInfo] the app can surface; the producer filters it by the live constraint state. */
object TipsCatalog {

    val tipsInfoList = listOf(
        TipsInfo.PostNotification,
        TipsInfo.AddQSTile,
        TipsInfo.RateApp,
    )
}
