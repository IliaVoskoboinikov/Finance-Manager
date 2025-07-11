package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.MdiDollar: ImageVector
    get() {
        if (_MdiDollar != null) {
            return _MdiDollar!!
        }
        _MdiDollar = ImageVector.Builder(
            name = "MdiDollar",
            defaultWidth = 24.dp,
            defaultHeight = 25.dp,
            viewportWidth = 24f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF1D1B20))) {
                moveTo(7f, 15.5f)
                horizontalLineTo(9f)
                curveTo(9f, 16.58f, 10.37f, 17.5f, 12f, 17.5f)
                curveTo(13.63f, 17.5f, 15f, 16.58f, 15f, 15.5f)
                curveTo(15f, 14.4f, 13.96f, 14f, 11.76f, 13.47f)
                curveTo(9.64f, 12.94f, 7f, 12.28f, 7f, 9.5f)
                curveTo(7f, 7.71f, 8.47f, 6.19f, 10.5f, 5.68f)
                verticalLineTo(3.5f)
                horizontalLineTo(13.5f)
                verticalLineTo(5.68f)
                curveTo(15.53f, 6.19f, 17f, 7.71f, 17f, 9.5f)
                horizontalLineTo(15f)
                curveTo(15f, 8.42f, 13.63f, 7.5f, 12f, 7.5f)
                curveTo(10.37f, 7.5f, 9f, 8.42f, 9f, 9.5f)
                curveTo(9f, 10.6f, 10.04f, 11f, 12.24f, 11.53f)
                curveTo(14.36f, 12.06f, 17f, 12.72f, 17f, 15.5f)
                curveTo(17f, 17.29f, 15.53f, 18.81f, 13.5f, 19.32f)
                verticalLineTo(21.5f)
                horizontalLineTo(10.5f)
                verticalLineTo(19.32f)
                curveTo(8.47f, 18.81f, 7f, 17.29f, 7f, 15.5f)
                close()
            }
        }.build()

        return _MdiDollar!!
    }

@Suppress("ObjectPropertyName")
private var _MdiDollar: ImageVector? = null
