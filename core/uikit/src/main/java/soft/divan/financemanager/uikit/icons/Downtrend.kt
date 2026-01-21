@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Downtrend: ImageVector
    get() {
        if (_DowntrenIcon != null) {
            return _DowntrenIcon!!
        }
        _DowntrenIcon = ImageVector.Builder(
            name = "Filled.DowntrenIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF2AE881))) {
                moveTo(21.262f, 18.796f)
                horizontalLineTo(19.363f)
                verticalLineTo(13.099f)
                horizontalLineTo(15.565f)
                verticalLineTo(18.796f)
                horizontalLineTo(14.299f)
                verticalLineTo(11.2f)
                horizontalLineTo(10.501f)
                verticalLineTo(18.796f)
                horizontalLineTo(9.235f)
                verticalLineTo(9.301f)
                horizontalLineTo(5.437f)
                verticalLineTo(18.796f)
                horizontalLineTo(3.538f)
                verticalLineTo(19.429f)
                horizontalLineTo(21.262f)
                verticalLineTo(18.796f)
                close()
            }
            path(fill = SolidColor(Color(0xFF2AE881))) {
                moveTo(16.065f, 11.624f)
                lineTo(19.585f, 10.174f)
                lineTo(17.888f, 6.775f)
                lineTo(17.319f, 7.053f)
                lineTo(18.502f, 9.434f)
                lineTo(5.551f, 4.572f)
                lineTo(5.323f, 5.167f)
                lineTo(18.281f, 10.029f)
                lineTo(15.825f, 11.035f)
                lineTo(16.065f, 11.624f)
                close()
            }
        }.build()

        return _DowntrenIcon!!
    }

@Suppress("ObjectPropertyName")
private var _DowntrenIcon: ImageVector? = null
