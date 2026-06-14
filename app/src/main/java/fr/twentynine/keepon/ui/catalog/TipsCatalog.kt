package fr.twentynine.keepon.ui.catalog

/** The full set of [TipInfo] the app can surface; the producer filters it by the live constraint state. */
object TipsCatalog {

    val tipsInfoList = listOf(
        TipInfo.PostNotification,
        TipInfo.AddQSTile,
        TipInfo.RateApp,
    )
}
