package fr.twentynine.keepon.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DismissActionRowView(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    text: String?,
    contentColor: Color,
    backgroundColor: Color,
    horizontalArrangement: Arrangement.Horizontal,
    contentVisible: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
    ) {
        if (contentVisible) {
            if (horizontalArrangement == Arrangement.End) {
                if (text != null) {
                    Text(
                        text = text,
                        color = contentColor,
                    )
                }
                if (icon != null) {
                    Icon(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        imageVector = icon,
                        tint = contentColor,
                        contentDescription = null,
                    )
                }
            } else {
                if (icon != null) {
                    Icon(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        imageVector = icon,
                        tint = contentColor,
                        contentDescription = null,
                    )
                }
                if (text != null) {
                    Text(
                        text = text,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
