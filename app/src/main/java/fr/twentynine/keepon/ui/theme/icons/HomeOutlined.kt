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
    Image(HomeOutlined, null)
}

val HomeOutlined: ImageVector
    get() {
        if (_HomeOutlined != null) {
            return _HomeOutlined!!
        }
        _HomeOutlined = ImageVector.Builder(
            name = "HomeOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(21f, 11.1f)
                lineToRelative(-8.4f, -7.5f)
                curveToRelative(-0.4f, -0.3f, -1f, -0.3f, -1.3f, 0f)
                lineTo(3f, 11.1f)
                curveTo(2.6f, 11.4f, 2.8f, 12f, 3.3f, 12f)
                horizontalLineTo(5f)
                verticalLineToRelative(7f)
                curveToRelative(0f, 0.5f, 0.5f, 1f, 1f, 1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.5f, 0f, 1f, -0.5f, 1f, -1f)
                verticalLineToRelative(-5f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(5f)
                curveToRelative(0f, 0.5f, 0.5f, 1f, 1f, 1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.5f, 0f, 1f, -0.5f, 1f, -1f)
                verticalLineToRelative(-7f)
                horizontalLineToRelative(1.7f)
                curveTo(21.2f, 12f, 21.4f, 11.4f, 21f, 11.1f)
                close()
                moveTo(17f, 18f)
                horizontalLineToRelative(-1f)
                verticalLineToRelative(-5.5f)
                curveToRelative(0f, -0.3f, -0.2f, -0.6f, -0.6f, -0.6f)
                horizontalLineTo(8.5f)
                curveTo(8.2f, 12f, 8f, 12.2f, 8f, 12.6f)
                verticalLineTo(18f)
                horizontalLineTo(7f)
                verticalLineToRelative(-7.5f)
                curveToRelative(0f, -0.2f, 0.1f, -0.3f, 0.2f, -0.4f)
                lineToRelative(4.5f, -4f)
                curveToRelative(0.2f, -0.2f, 0.5f, -0.2f, 0.7f, 0f)
                lineToRelative(4.5f, 4f)
                curveToRelative(0.1f, 0.1f, 0.2f, 0.3f, 0.2f, 0.4f)
                verticalLineTo(18f)
                horizontalLineTo(17f)
                close()
            }
        }.build()

        return _HomeOutlined!!
    }

@Suppress("ObjectPropertyName")
private var _HomeOutlined: ImageVector? = null
