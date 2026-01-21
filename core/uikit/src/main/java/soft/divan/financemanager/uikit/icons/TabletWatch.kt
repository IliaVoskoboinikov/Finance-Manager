@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.TabletWatch: ImageVector
    get() {
        if (_TabletWattch != null) {
            return _TabletWattch!!
        }
        _TabletWattch = ImageVector.Builder(
            name = "Filled.TabletWattch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(21f, 11.11f)
                verticalLineTo(5f)
                curveTo(21f, 4.47f, 20.789f, 3.961f, 20.414f, 3.586f)
                curveTo(20.039f, 3.211f, 19.53f, 3f, 19f, 3f)
                horizontalLineTo(14.82f)
                curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
                curveTo(10.7f, 1f, 9.6f, 1.84f, 9.18f, 3f)
                horizontalLineTo(5f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 19.53f, 3.211f, 20.039f, 3.586f, 20.414f)
                curveTo(3.961f, 20.789f, 4.47f, 21f, 5f, 21f)
                horizontalLineTo(11.11f)
                curveTo(12.37f, 22.24f, 14.09f, 23f, 16f, 23f)
                curveTo(19.87f, 23f, 23f, 19.87f, 23f, 16f)
                curveTo(23f, 14.09f, 22.24f, 12.37f, 21f, 11.11f)
                close()
                moveTo(12f, 3f)
                curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
                curveTo(13f, 4.55f, 12.55f, 5f, 12f, 5f)
                curveTo(11.45f, 5f, 11f, 4.55f, 11f, 4f)
                curveTo(11f, 3.45f, 11.45f, 3f, 12f, 3f)
                close()
                moveTo(5f, 19f)
                verticalLineTo(5f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineTo(17f)
                verticalLineTo(5f)
                horizontalLineTo(19f)
                verticalLineTo(9.68f)
                curveTo(18.09f, 9.25f, 17.08f, 9f, 16f, 9f)
                horizontalLineTo(7f)
                verticalLineTo(11f)
                horizontalLineTo(11.1f)
                curveTo(10.5f, 11.57f, 10.04f, 12.25f, 9.68f, 13f)
                horizontalLineTo(7f)
                verticalLineTo(15f)
                horizontalLineTo(9.08f)
                curveTo(9.03f, 15.33f, 9f, 15.66f, 9f, 16f)
                curveTo(9f, 17.08f, 9.25f, 18.09f, 9.68f, 19f)
                horizontalLineTo(5f)
                close()
                moveTo(16f, 21f)
                curveTo(13.24f, 21f, 11f, 18.76f, 11f, 16f)
                curveTo(11f, 13.24f, 13.24f, 11f, 16f, 11f)
                curveTo(18.76f, 11f, 21f, 13.24f, 21f, 16f)
                curveTo(21f, 18.76f, 18.76f, 21f, 16f, 21f)
                close()
                moveTo(16.5f, 16.25f)
                lineTo(19.36f, 17.94f)
                lineTo(18.61f, 19.16f)
                lineTo(15f, 17f)
                verticalLineTo(12f)
                horizontalLineTo(16.5f)
                verticalLineTo(16.25f)
                close()
            }
        }.build()

        return _TabletWattch!!
    }

@Suppress("ObjectPropertyName")
private var _TabletWattch: ImageVector? = null
