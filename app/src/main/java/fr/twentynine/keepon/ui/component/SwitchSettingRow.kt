package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.StyleSwitchRowVerticalPadding

/**
 * A settings switch row with a [title] and an optional [subtitle], the switch sitting on the shared
 * left rail (via [LabeledControlRow]) so it lines up with the other controls in the card. Tapping
 * anywhere on the row toggles [checked]. Reusable across cards (e.g. the animation toggle, the
 * "outline only" toggle). Title and subtitle use the theme typography (titleSmall / bodyMedium), the
 * subtitle tinted with the secondary content color.
 */
@Composable
fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    verticalPadding: Dp = StyleSwitchRowVerticalPadding,
) {
    LabeledControlRow(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        enabled = enabled,
        verticalPadding = verticalPadding,
        leading = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        },
        label = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                if (subtitle != null) {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        },
    )
}
