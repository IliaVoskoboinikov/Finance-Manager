@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.MdiRuble: ImageVector
    get() {
        if (_MdiRuble != null) {
            return _MdiRuble!!
        }
        _MdiRuble = ImageVector.Builder(
            name = "MdiRuble",
            defaultWidth = 24.dp,
            defaultHeight = 25.dp,
            viewportWidth = 24f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(13.5f, 3.5f)
                horizontalLineTo(7f)
                verticalLineTo(12.5f)
                horizontalLineTo(5f)
                verticalLineTo(14.5f)
                horizontalLineTo(7f)
                verticalLineTo(16.5f)
                horizontalLineTo(5f)
                verticalLineTo(18.5f)
                horizontalLineTo(7f)
                verticalLineTo(21.5f)
                horizontalLineTo(9f)
                verticalLineTo(18.5f)
                horizontalLineTo(13f)
                verticalLineTo(16.5f)
                horizontalLineTo(9f)
                verticalLineTo(14.5f)
                horizontalLineTo(13.5f)
                curveTo(16.54f, 14.5f, 19f, 12.04f, 19f, 9f)
                curveTo(19f, 5.96f, 16.54f, 3.5f, 13.5f, 3.5f)
                close()
                moveTo(13.5f, 12.5f)
                horizontalLineTo(9f)
                verticalLineTo(5.5f)
                horizontalLineTo(13.5f)
                curveTo(15.43f, 5.5f, 17f, 7.07f, 17f, 9f)
                curveTo(17f, 10.93f, 15.43f, 12.5f, 13.5f, 12.5f)
                close()
            }
        }.build()

        return _MdiRuble!!
    }

@Suppress("ObjectPropertyName")
private var _MdiRuble: ImageVector? = null
