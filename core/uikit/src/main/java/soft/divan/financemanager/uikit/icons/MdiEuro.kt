@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.MdiEuro: ImageVector
    get() {
        if (_MdiEuro != null) {
            return _MdiEuro!!
        }
        _MdiEuro = ImageVector.Builder(
            name = "MdiEuro",
            defaultWidth = 24.dp,
            defaultHeight = 25.dp,
            viewportWidth = 24f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF1D1B20))) {
                moveTo(15f, 19f)
                curveTo(12.5f, 19f, 10.32f, 17.58f, 9.24f, 15.5f)
                horizontalLineTo(15f)
                lineTo(16f, 13.5f)
                horizontalLineTo(8.58f)
                curveTo(8.53f, 13.17f, 8.5f, 12.84f, 8.5f, 12.5f)
                curveTo(8.5f, 12.16f, 8.53f, 11.83f, 8.58f, 11.5f)
                horizontalLineTo(15f)
                lineTo(16f, 9.5f)
                horizontalLineTo(9.24f)
                curveTo(9.788f, 8.445f, 10.614f, 7.561f, 11.63f, 6.944f)
                curveTo(12.646f, 6.326f, 13.811f, 6f, 15f, 6f)
                curveTo(16.61f, 6f, 18.09f, 6.59f, 19.23f, 7.57f)
                lineTo(21f, 5.8f)
                curveTo(19.353f, 4.318f, 17.216f, 3.498f, 15f, 3.5f)
                curveTo(11.08f, 3.5f, 7.76f, 6f, 6.5f, 9.5f)
                horizontalLineTo(3f)
                lineTo(2f, 11.5f)
                horizontalLineTo(6.06f)
                curveTo(6f, 11.83f, 6f, 12.16f, 6f, 12.5f)
                curveTo(6f, 12.84f, 6f, 13.17f, 6.06f, 13.5f)
                horizontalLineTo(3f)
                lineTo(2f, 15.5f)
                horizontalLineTo(6.5f)
                curveTo(7.76f, 19f, 11.08f, 21.5f, 15f, 21.5f)
                curveTo(17.31f, 21.5f, 19.41f, 20.63f, 21f, 19.2f)
                lineTo(19.22f, 17.43f)
                curveTo(18.09f, 18.41f, 16.62f, 19f, 15f, 19f)
                close()
            }
        }.build()

        return _MdiEuro!!
    }

@Suppress("ObjectPropertyName")
private var _MdiEuro: ImageVector? = null
