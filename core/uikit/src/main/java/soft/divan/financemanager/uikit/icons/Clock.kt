@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Clock: ImageVector
    get() {
        if (_Clock != null) {
            return _Clock!!
        }
        _Clock = ImageVector.Builder(
            name = "Filled.Clock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(13.5f, 8f)
                horizontalLineTo(12f)
                verticalLineTo(13f)
                lineTo(16.28f, 15.54f)
                lineTo(17f, 14.33f)
                lineTo(13.5f, 12.25f)
                verticalLineTo(8f)
                close()
                moveTo(13f, 3f)
                curveTo(10.613f, 3f, 8.324f, 3.948f, 6.636f, 5.636f)
                curveTo(4.948f, 7.324f, 4f, 9.613f, 4f, 12f)
                horizontalLineTo(1f)
                lineTo(4.96f, 16.03f)
                lineTo(9f, 12f)
                horizontalLineTo(6f)
                curveTo(6f, 10.144f, 6.738f, 8.363f, 8.05f, 7.05f)
                curveTo(9.363f, 5.738f, 11.144f, 5f, 13f, 5f)
                curveTo(14.856f, 5f, 16.637f, 5.738f, 17.95f, 7.05f)
                curveTo(19.263f, 8.363f, 20f, 10.144f, 20f, 12f)
                curveTo(20f, 13.856f, 19.263f, 15.637f, 17.95f, 16.95f)
                curveTo(16.637f, 18.263f, 14.856f, 19f, 13f, 19f)
                curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
                lineTo(6.64f, 18.36f)
                curveTo(7.472f, 19.2f, 8.462f, 19.867f, 9.554f, 20.32f)
                curveTo(10.646f, 20.773f, 11.818f, 21.004f, 13f, 21f)
                curveTo(15.387f, 21f, 17.676f, 20.052f, 19.364f, 18.364f)
                curveTo(21.052f, 16.676f, 22f, 14.387f, 22f, 12f)
                curveTo(22f, 9.613f, 21.052f, 7.324f, 19.364f, 5.636f)
                curveTo(17.676f, 3.948f, 15.387f, 3f, 13f, 3f)
                close()
            }
        }.build()

        return _Clock!!
    }

@Suppress("ObjectPropertyName")
private var _Clock: ImageVector? = null
// Revue me>>
