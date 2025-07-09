package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Arrow: ImageVector
    get() {
        if (_Arrow != null) {
            return _Arrow!!
        }
        _Arrow = ImageVector.Builder(
            name = "Filled.Arrow",
            defaultWidth = 24.dp,
            defaultHeight = 25.dp,
            viewportWidth = 24f,
            viewportHeight = 25f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF3C3C43)),
                fillAlpha = 0.3f
            ) {
                moveTo(16.35f, 12.507f)
                curveTo(16.35f, 12.651f, 16.322f, 12.783f, 16.267f, 12.905f)
                curveTo(16.211f, 13.027f, 16.125f, 13.146f, 16.009f, 13.262f)
                lineTo(11.037f, 18.184f)
                curveTo(10.854f, 18.367f, 10.633f, 18.458f, 10.373f, 18.458f)
                curveTo(10.102f, 18.458f, 9.872f, 18.367f, 9.684f, 18.184f)
                curveTo(9.496f, 17.996f, 9.402f, 17.769f, 9.402f, 17.504f)
                curveTo(9.402f, 17.238f, 9.504f, 17.003f, 9.709f, 16.798f)
                lineTo(14.067f, 12.507f)
                lineTo(9.709f, 8.215f)
                curveTo(9.504f, 8.01f, 9.402f, 7.775f, 9.402f, 7.51f)
                curveTo(9.402f, 7.244f, 9.496f, 7.02f, 9.684f, 6.837f)
                curveTo(9.872f, 6.649f, 10.102f, 6.555f, 10.373f, 6.555f)
                curveTo(10.633f, 6.555f, 10.854f, 6.646f, 11.037f, 6.829f)
                lineTo(16.009f, 11.751f)
                curveTo(16.236f, 11.967f, 16.35f, 12.219f, 16.35f, 12.507f)
                close()
            }
        }.build()

        return _Arrow!!
    }

@Suppress("ObjectPropertyName")
private var _Arrow: ImageVector? = null
