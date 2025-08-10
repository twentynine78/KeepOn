package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.data.local.TipsInfo

object TipsInfoRepository {

    val tipsInfoList = listOf(
        TipsInfo.PostNotification,
        TipsInfo.AddQSTile,
        TipsInfo.RateApp,
    )
}
