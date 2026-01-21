@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Settings: ImageVector
    get() {
        if (_Settings != null) {
            return _Settings!!
        }
        _Settings = ImageVector.Builder(
            name = "Filled.Settings",
            defaultWidth = 25.dp,
            defaultHeight = 24.dp,
            viewportWidth = 25f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(12.029f, 2.858f)
                lineTo(19.432f, 7.143f)
                verticalLineTo(15.715f)
                lineTo(12.029f, 20f)
                lineTo(4.626f, 15.715f)
                verticalLineTo(7.143f)
                lineTo(12.029f, 2.858f)
                close()
                moveTo(12.029f, 13.767f)
                curveTo(12.649f, 13.767f, 13.244f, 13.521f, 13.682f, 13.082f)
                curveTo(14.12f, 12.644f, 14.367f, 12.049f, 14.367f, 11.429f)
                curveTo(14.367f, 10.809f, 14.12f, 10.215f, 13.682f, 9.776f)
                curveTo(13.244f, 9.338f, 12.649f, 9.091f, 12.029f, 9.091f)
                curveTo(11.409f, 9.091f, 10.814f, 9.338f, 10.376f, 9.776f)
                curveTo(9.938f, 10.215f, 9.691f, 10.809f, 9.691f, 11.429f)
                curveTo(9.691f, 12.049f, 9.938f, 12.644f, 10.376f, 13.082f)
                curveTo(10.814f, 13.521f, 11.409f, 13.767f, 12.029f, 13.767f)
                close()
            }
        }.build()

        return _Settings!!
    }

@Suppress("ObjectPropertyName")
private var _Settings: ImageVector? = null
