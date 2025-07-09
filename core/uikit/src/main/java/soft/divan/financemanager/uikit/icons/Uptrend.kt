package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Uptrend: ImageVector
    get() {
        if (_Uptrend != null) {
            return _Uptrend!!
        }
        _Uptrend = ImageVector.Builder(
            name = "Filled.Uptrend",
            defaultWidth = 25.dp,
            defaultHeight = 24.dp,
            viewportWidth = 25f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(20.306f, 18.85f)
                horizontalLineTo(18.569f)
                verticalLineTo(10.165f)
                horizontalLineTo(15.095f)
                verticalLineTo(18.85f)
                horizontalLineTo(13.937f)
                verticalLineTo(11.902f)
                horizontalLineTo(10.463f)
                verticalLineTo(18.85f)
                horizontalLineTo(9.305f)
                verticalLineTo(13.639f)
                horizontalLineTo(5.831f)
                verticalLineTo(18.85f)
                horizontalLineTo(4.094f)
                verticalLineTo(19.429f)
                horizontalLineTo(20.306f)
                verticalLineTo(18.85f)
                close()
            }
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(17.22f, 9.001f)
                lineTo(18.771f, 5.892f)
                lineTo(15.552f, 4.572f)
                lineTo(15.332f, 5.105f)
                lineTo(17.579f, 6.031f)
                lineTo(5.727f, 10.472f)
                lineTo(5.935f, 11.016f)
                lineTo(17.787f, 6.57f)
                lineTo(16.699f, 8.747f)
                lineTo(17.22f, 9.001f)
                close()
            }
        }.build()

        return _Uptrend!!
    }

@Suppress("ObjectPropertyName")
private var _Uptrend: ImageVector? = null
