package fr.twentynine.keepon.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
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
    val iconImageVector: ImageVector,
    val constraint: (tipsConstraintState: TipsConstraintState) -> Boolean,
) {
    data object PostNotification : TipsInfo(
        id = 100,
        titleId = R.string.tip_general_notification_title,
        textId = R.string.tip_general_notification_text,
        buttonTextId = R.string.tip_general_notification_action_button_text,
        buttonDismissTextId = R.string.tip_general_notification_dismiss_button_text,
        iconImageVector = Icons.Rounded.NotificationsActive,
        buttonAction = MainUIEvent.RequestPostNotification,
        constraint = { uiState -> !uiState.canPostNotification }
    )
    data object AddQSTile : TipsInfo(
        id = 200,
        titleId = R.string.tip_qstile_title,
        textId = R.string.tip_qstile_text,
        buttonTextId = R.string.tip_qstile_action_button_text,
        buttonDismissTextId = R.string.tip_qstile_dismiss_button_text,
        iconImageVector = Icons.Rounded.AddCircleOutline,
        buttonAction = MainUIEvent.RequestAddTileService,
        constraint = { uiState -> !uiState.tileServiceIsAdded }
    )
    data object RateApp : TipsInfo(
        id = 300,
        titleId = R.string.tip_rateapp_title,
        textId = R.string.tip_rateapp_text,
        buttonTextId = R.string.tip_rateapp_action_button_text,
        buttonDismissTextId = R.string.tip_rateapp_dismiss_button_text,
        iconImageVector = Icons.Rounded.StarOutline,
        buttonAction = MainUIEvent.RequestAppRate,
        constraint = { uiState -> uiState.showRateApp }
    )
}
