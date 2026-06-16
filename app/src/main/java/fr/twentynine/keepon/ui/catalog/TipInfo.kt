package fr.twentynine.keepon.ui.catalog

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.state.TipsConstraintState

/**
 * A contextual tip shown on the Home screen: its texts/icon, optional primary [buttonAction], and a
 * [constraint] predicate over the current [TipsConstraintState] that decides whether it is relevant
 * right now. The stable [id] is what gets persisted when the user dismisses it.
 *
 * A tip with neither [buttonAction] nor [buttonDismissTextId] renders no button row (e.g. the
 * functional [SelectTimeout] advisory, which the producer surfaces/removes on its own).
 */
@Immutable
sealed class TipInfo(
    val id: Int,
    val titleId: Int,
    val textId: Int,
    val iconImageVector: ImageVector,
    val buttonAction: TipAction? = null,
    val buttonTextId: Int? = null,
    val buttonDismissTextId: Int? = null,
    val isWarning: Boolean = false,
    val constraint: (tipsConstraintState: TipsConstraintState) -> Boolean = { false },
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

    /**
     * Advisory shown when no timeout other than the always-selected default is enabled, so there is
     * nothing to cycle to. No buttons and not dismissible: the producer adds it / removes it from the
     * tips list directly from the selection state, so its [constraint] is unused.
     */
    data object SelectTimeout : TipInfo(
        id = 400,
        titleId = R.string.select_timeouts_warning_title,
        textId = R.string.select_timeouts_warning_text,
        iconImageVector = Icons.Outlined.WarningAmber,
        isWarning = true,
    )
}
