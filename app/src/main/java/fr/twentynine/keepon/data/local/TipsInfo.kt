package fr.twentynine.keepon.data.local

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.TipsConstraintState

@Immutable
sealed class TipsInfo(
    val id: Int,
    val titleId: Int,
    val textId: Int,
    val buttonAction: MainUIEvent,
    val buttonTextId: Int,
    val buttonDismissTextId: Int,
    val iconResourceId: Int,
    val constraint: (tipsConstraintState: TipsConstraintState) -> Boolean,
) {
    data object PostNotification : TipsInfo(
        id = 100,
        titleId = R.string.tip_general_notification_title,
        textId = R.string.tip_general_notification_text,
        buttonTextId = R.string.tip_general_notification_action_button_text,
        buttonDismissTextId = R.string.tip_general_notification_dismiss_button_text,
        iconResourceId = R.drawable.ic_notifications_active,
        buttonAction = MainUIEvent.RequestPostNotification,
        constraint = { uiState -> !uiState.canPostNotification }
    )
    data object AddQSTile : TipsInfo(
        id = 200,
        titleId = R.string.tip_qstile_title,
        textId = R.string.tip_qstile_text,
        buttonTextId = R.string.tip_qstile_action_button_text,
        buttonDismissTextId = R.string.tip_qstile_dismiss_button_text,
        iconResourceId = R.drawable.ic_add,
        buttonAction = MainUIEvent.RequestAddTileService,
        constraint = { uiState -> !uiState.tileServiceIsAdded }
    )
    data object RateApp : TipsInfo(
        id = 300,
        titleId = R.string.tip_rateapp_title,
        textId = R.string.tip_rateapp_text,
        buttonTextId = R.string.tip_rateapp_action_button_text,
        buttonDismissTextId = R.string.tip_rateapp_dismiss_button_text,
        iconResourceId = R.drawable.ic_rate,
        buttonAction = MainUIEvent.RequestAppRate,
        constraint = { uiState -> uiState.showRateApp }
    )
}
