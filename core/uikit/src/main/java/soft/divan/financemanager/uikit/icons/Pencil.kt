package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Pencil: ImageVector
    get() {
        if (_TrailingIcon != null) {
            return _TrailingIcon!!
        }
        _TrailingIcon = ImageVector.Builder(
            name = "Filled.TrailingIcon",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(24f, 4f)
                    lineTo(24f, 4f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 24f)
                    lineTo(44f, 24f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 44f)
                    lineTo(24f, 44f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 24f)
                    lineTo(4f, 24f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 4f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF49454F))) {
                    moveTo(17f, 31f)
                    horizontalLineTo(18.425f)
                    lineTo(28.2f, 21.225f)
                    lineTo(26.775f, 19.8f)
                    lineTo(17f, 29.575f)
                    verticalLineTo(31f)
                    close()
                    moveTo(15f, 33f)
                    verticalLineTo(28.75f)
                    lineTo(28.2f, 15.575f)
                    curveTo(28.4f, 15.392f, 28.621f, 15.25f, 28.862f, 15.15f)
                    curveTo(29.104f, 15.05f, 29.358f, 15f, 29.625f, 15f)
                    curveTo(29.892f, 15f, 30.15f, 15.05f, 30.4f, 15.15f)
                    curveTo(30.65f, 15.25f, 30.867f, 15.4f, 31.05f, 15.6f)
                    lineTo(32.425f, 17f)
                    curveTo(32.625f, 17.183f, 32.771f, 17.4f, 32.862f, 17.65f)
                    curveTo(32.954f, 17.9f, 33f, 18.15f, 33f, 18.4f)
                    curveTo(33f, 18.667f, 32.954f, 18.921f, 32.862f, 19.163f)
                    curveTo(32.771f, 19.404f, 32.625f, 19.625f, 32.425f, 19.825f)
                    lineTo(19.25f, 33f)
                    horizontalLineTo(15f)
                    close()
                    moveTo(27.475f, 20.525f)
                    lineTo(26.775f, 19.8f)
                    lineTo(28.2f, 21.225f)
                    lineTo(27.475f, 20.525f)
                    close()
                }
            }
        }.build()

        return _TrailingIcon!!
    }

@Suppress("ObjectPropertyName")
private var _TrailingIcon: ImageVector? = null
