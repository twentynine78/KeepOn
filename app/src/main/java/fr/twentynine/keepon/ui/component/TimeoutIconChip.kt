package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.theme.CHIP_BACKGROUND_ALPHA
import fr.twentynine.keepon.ui.theme.CHIP_BORDER_ALPHA
import fr.twentynine.keepon.ui.theme.KeepOnChipShape
import fr.twentynine.keepon.ui.theme.KeepOnChipSize
import fr.twentynine.keepon.ui.util.rememberTimeoutIconModel

/**
 * The rounded chip that shows a generated timeout icon, shared by the home and Tasker rows.
 * Alignment/positioning is left to the caller via [modifier].
 */
@Composable
fun TimeoutIconChip(
    screenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val imageData = remember(screenTimeout, timeoutIconStyle) {
        TimeoutIconData(screenTimeout, TimeoutIconSize.MEDIUM, timeoutIconStyle)
    }
    val imageModel = rememberTimeoutIconModel(imageData)

    Box(
        modifier = modifier
            .clip(KeepOnChipShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = CHIP_BACKGROUND_ALPHA))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = CHIP_BORDER_ALPHA),
                shape = KeepOnChipShape,
            )
            .size(KeepOnChipSize),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            modifier = Modifier.size(20.dp),
            model = imageModel,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
            contentDescription = contentDescription,
        )
    }
}
