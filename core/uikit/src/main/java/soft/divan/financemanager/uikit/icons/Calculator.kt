package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Calculator: ImageVector
    get() {
        if (_Account != null) {
            return _Account!!
        }
        _Account = ImageVector.Builder(
            name = "Filled.Account",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(6.738f, 4.572f)
                curveTo(6.057f, 4.572f, 5.5f, 5.129f, 5.5f, 5.81f)
                verticalLineTo(18.191f)
                curveTo(5.5f, 18.872f, 6.057f, 19.429f, 6.738f, 19.429f)
                horizontalLineTo(17.262f)
                curveTo(17.943f, 19.429f, 18.5f, 18.872f, 18.5f, 18.191f)
                verticalLineTo(5.81f)
                curveTo(18.5f, 5.129f, 17.943f, 4.572f, 17.262f, 4.572f)
                horizontalLineTo(6.738f)
                close()
                moveTo(9.214f, 17.572f)
                horizontalLineTo(7.357f)
                verticalLineTo(15.715f)
                horizontalLineTo(9.214f)
                verticalLineTo(17.572f)
                close()
                moveTo(9.214f, 15.096f)
                horizontalLineTo(7.357f)
                verticalLineTo(13.239f)
                horizontalLineTo(9.214f)
                verticalLineTo(15.096f)
                close()
                moveTo(9.214f, 12.62f)
                horizontalLineTo(7.357f)
                verticalLineTo(10.762f)
                horizontalLineTo(9.214f)
                verticalLineTo(12.62f)
                close()
                moveTo(11.691f, 17.572f)
                horizontalLineTo(9.833f)
                verticalLineTo(15.715f)
                horizontalLineTo(11.691f)
                verticalLineTo(17.572f)
                close()
                moveTo(11.691f, 15.096f)
                horizontalLineTo(9.833f)
                verticalLineTo(13.239f)
                horizontalLineTo(11.691f)
                verticalLineTo(15.096f)
                close()
                moveTo(11.691f, 12.62f)
                horizontalLineTo(9.833f)
                verticalLineTo(10.762f)
                horizontalLineTo(11.691f)
                verticalLineTo(12.62f)
                close()
                moveTo(14.167f, 17.572f)
                horizontalLineTo(12.309f)
                verticalLineTo(15.715f)
                horizontalLineTo(14.167f)
                verticalLineTo(17.572f)
                close()
                moveTo(14.167f, 15.096f)
                horizontalLineTo(12.309f)
                verticalLineTo(13.239f)
                horizontalLineTo(14.167f)
                verticalLineTo(15.096f)
                close()
                moveTo(14.167f, 12.62f)
                horizontalLineTo(12.309f)
                verticalLineTo(10.762f)
                horizontalLineTo(14.167f)
                verticalLineTo(12.62f)
                close()
                moveTo(16.643f, 17.572f)
                horizontalLineTo(14.786f)
                verticalLineTo(13.239f)
                horizontalLineTo(16.643f)
                verticalLineTo(17.572f)
                close()
                moveTo(16.643f, 12.62f)
                horizontalLineTo(14.786f)
                verticalLineTo(10.762f)
                horizontalLineTo(16.643f)
                verticalLineTo(12.62f)
                close()
                moveTo(16.643f, 9.524f)
                horizontalLineTo(7.357f)
                verticalLineTo(6.429f)
                horizontalLineTo(16.643f)
                verticalLineTo(9.524f)
                close()
            }
        }.build()

        return _Account!!
    }

@Suppress("ObjectPropertyName")
private var _Account: ImageVector? = null
