package fr.twentynine.keepon.ui.theme.icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun VectorPreview() {
    Image(HomeFilled, null)
}

val HomeFilled: ImageVector
    get() {
        if (_HomeFilled != null) {
            return _HomeFilled!!
        }
        _HomeFilled = ImageVector.Builder(
            name = "HomeFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(10f, 19f)
                verticalLineToRelative(-5f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(5f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                verticalLineToRelative(-7f)
                horizontalLineToRelative(1.7f)
                curveToRelative(0.46f, 0f, 0.68f, -0.57f, 0.33f, -0.87f)
                lineTo(12.67f, 3.6f)
                curveToRelative(-0.38f, -0.34f, -0.96f, -0.34f, -1.34f, 0f)
                lineToRelative(-8.36f, 7.53f)
                curveToRelative(-0.34f, 0.3f, -0.13f, 0.87f, 0.33f, 0.87f)
                horizontalLineTo(5f)
                verticalLineToRelative(7f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                close()
            }
        }.build()

        return _HomeFilled!!
    }

@Suppress("ObjectPropertyName")
private var _HomeFilled: ImageVector? = null
