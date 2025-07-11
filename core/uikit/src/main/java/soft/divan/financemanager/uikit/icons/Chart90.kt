package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Chart90: ImageVector
    get() {
        if (_Chart90 != null) {
            return _Chart90!!
        }
        _Chart90 = ImageVector.Builder(
            name = "Filled.Chart90",
            defaultWidth = 25.dp,
            defaultHeight = 24.dp,
            viewportWidth = 25f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(5.075f, 4f)
                    horizontalLineToRelative(14.307f)
                    verticalLineToRelative(14.857f)
                    horizontalLineToRelative(-14.307f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF49454F))) {
                    moveTo(5.625f, 18.307f)
                    horizontalLineTo(19.382f)
                    verticalLineTo(14.455f)
                    horizontalLineTo(5.625f)
                    verticalLineTo(13.355f)
                    horizontalLineTo(16.08f)
                    verticalLineTo(9.503f)
                    horizontalLineTo(5.625f)
                    verticalLineTo(8.403f)
                    horizontalLineTo(13.329f)
                    verticalLineTo(4.551f)
                    horizontalLineTo(5.625f)
                    verticalLineTo(4f)
                    horizontalLineTo(5.075f)
                    verticalLineTo(18.858f)
                    horizontalLineTo(5.625f)
                    verticalLineTo(18.307f)
                    close()
                }
            }
        }.build()

        return _Chart90!!
    }

@Suppress("ObjectPropertyName")
private var _Chart90: ImageVector? = null
