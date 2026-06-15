package fr.twentynine.keepon.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.theme.CardHeaderDescBottomPadding
import fr.twentynine.keepon.ui.theme.CardHeaderDescHorizontalPadding
import fr.twentynine.keepon.ui.theme.CardHeaderInfoIconSpacing
import fr.twentynine.keepon.ui.theme.CardHeaderPadding
import fr.twentynine.keepon.ui.theme.CardHeaderTitleSpacing

private const val CONTENT_COLOR_FRACTION = 0.5f

/**
 * Section header inside a settings card: an optional leading icon and a [title]. When [descText] is
 * given, an info icon appears and tapping the row toggles an expandable description below it.
 */
@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconVector: ImageVector? = null,
    iconSize: Int = 16,
    title: String,
    descText: String? = null
) {
    val infoVisible = rememberSaveable { mutableStateOf(false) }
    val moreInfoContentDesc = stringResource(R.string.more_info_icon_desc)

    val secondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val contentColor = remember(secondaryContainerColor, onSurfaceColor) {
        lerp(
            start = secondaryContainerColor,
            stop = onSurfaceColor,
            fraction = CONTENT_COLOR_FRACTION,
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(bottom = CardHeaderPadding, start = CardHeaderPadding, end = CardHeaderPadding)
                .clickable(
                    onClick = {
                        if (descText != null) {
                            infoVisible.value = !infoVisible.value
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                icon != null -> Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize.dp),
                )
                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier
                    .padding(start = CardHeaderTitleSpacing)
                    .weight(1f)
            )
            if (descText != null) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = moreInfoContentDesc,
                    tint = contentColor,
                    modifier = Modifier
                        .padding(start = CardHeaderInfoIconSpacing)
                        .size(14.dp),
                )
            }
        }
        AnimatedVisibility(
            visible = infoVisible.value && descText != null,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            descText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(
                            bottom = CardHeaderDescBottomPadding,
                            start = CardHeaderDescHorizontalPadding,
                            end = CardHeaderDescHorizontalPadding,
                        ),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
