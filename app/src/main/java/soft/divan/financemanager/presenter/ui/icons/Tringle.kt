package soft.divan.financemanager.presenter.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Triangle: ImageVector
    get() {
        if (_Tringle != null) {
            return _Tringle!!
        }
        _Tringle = ImageVector.Builder(
            name = "Filled.Triangle",
            defaultWidth = 24.dp,
            defaultHeight = 25.dp,
            viewportWidth = 24f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(10f, 17.5f)
                verticalLineTo(7.5f)
                lineTo(15f, 12.5f)
                lineTo(10f, 17.5f)
                close()
            }
        }.build()

        return _Tringle!!
    }

@Suppress("ObjectPropertyName")
private var _Tringle: ImageVector? = null
