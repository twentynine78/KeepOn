package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
private val ToolTipCaretShape = TooltipDefaults.caretShape(DpSize(16.dp, 12.dp))
private val SpacingBetweenTooltipAndAnchor = 22.dp

/**
 * A bordered rich tooltip anchored to the left of [content], styled consistently across the app
 * (surface-variant container, outlined border, caret). Shared by the home and Tasker rows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepOnRichTooltip(
    text: String,
    tooltipState: TooltipState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Left,
            spacingBetweenTooltipAndAnchor = SpacingBetweenTooltipAndAnchor,
        ),
        tooltip = {
            RichTooltip(
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = TooltipDefaults.richTooltipContainerShape,
                ),
                maxWidth = TooltipDefaults.plainTooltipMaxWidth,
                shape = TooltipDefaults.richTooltipContainerShape,
                caretShape = ToolTipCaretShape,
                colors = TooltipDefaults.richTooltipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            ) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        state = tooltipState,
        content = content,
    )
}
