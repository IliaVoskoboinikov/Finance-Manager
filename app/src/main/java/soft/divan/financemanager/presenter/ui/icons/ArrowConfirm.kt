package soft.divan.financemanager.presenter.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.ArrowConfirm: ImageVector
    get() {
        if (_ArrowConfirm != null) {
            return _ArrowConfirm!!
        }
        _ArrowConfirm = ImageVector.Builder(
            name = "Filled.ArrowConfirm",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF49454F))) {
                moveTo(9.55f, 18f)
                lineTo(3.85f, 12.3f)
                lineTo(5.275f, 10.875f)
                lineTo(9.55f, 15.15f)
                lineTo(18.725f, 5.975f)
                lineTo(20.15f, 7.4f)
                lineTo(9.55f, 18f)
                close()
            }
        }.build()

        return _ArrowConfirm!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowConfirm: ImageVector? = null
