package fr.twentynine.keepon.ui.catalog

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.state.TipsConstraintState

/**
 * A contextual tip shown on the Home screen: its texts/icon, primary [buttonAction], and a
 * [constraint] predicate over the current [TipsConstraintState] that decides whether it is relevant
 * right now. The stable [id] is what gets persisted when the user dismisses it.
 */
@Immutable
sealed class TipInfo(
    val id: Int,
    val titleId: Int,
    val textId: Int,
    val buttonAction: TipAction,
    val buttonTextId: Int,
    val buttonDismissTextId: Int,
    val iconImageVector: ImageVector,
    val constraint: (tipsConstraintState: TipsConstraintState) -> Boolean,
) {
    data object PostNotification : TipInfo(
        id = 100,
        titleId = R.string.tip_general_notification_title,
        textId = R.string.tip_general_notification_text,
        buttonTextId = R.string.tip_general_notification_action_button_text,
        buttonDismissTextId = R.string.tip_general_notification_dismiss_button_text,
        iconImageVector = Icons.Rounded.NotificationsActive,
        buttonAction = TipAction.RequestPostNotification,
        constraint = { uiState -> !uiState.canPostNotification }
    )
    data object AddQSTile : TipInfo(
        id = 200,
        titleId = R.string.tip_qstile_title,
        textId = R.string.tip_qstile_text,
        buttonTextId = R.string.tip_qstile_action_button_text,
        buttonDismissTextId = R.string.tip_qstile_dismiss_button_text,
        iconImageVector = Icons.Rounded.AddCircleOutline,
        buttonAction = TipAction.RequestAddTileService,
        constraint = { uiState ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.tileServiceIsAdded
        },
    )
    data object RateApp : TipInfo(
        id = 300,
        titleId = R.string.tip_rateapp_title,
        textId = R.string.tip_rateapp_text,
        buttonTextId = R.string.tip_rateapp_action_button_text,
        buttonDismissTextId = R.string.tip_rateapp_dismiss_button_text,
        iconImageVector = Icons.Rounded.StarOutline,
        buttonAction = TipAction.RequestAppRate,
        constraint = { uiState -> uiState.showRateApp }
    )
}
