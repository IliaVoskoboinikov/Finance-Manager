@file:Suppress("MagicNumber")

package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.ArrowBack: ImageVector
    get() {
        if (_ArrowBack != null) {
            return _ArrowBack!!
        }
        _ArrowBack = ImageVector.Builder(
            name = "Filled.ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF1D1B20))) {
                moveTo(7.825f, 13f)
                lineTo(13.425f, 18.6f)
                lineTo(12f, 20f)
                lineTo(4f, 12f)
                lineTo(12f, 4f)
                lineTo(13.425f, 5.4f)
                lineTo(7.825f, 11f)
                horizontalLineTo(20f)
                verticalLineTo(13f)
                horizontalLineTo(7.825f)
                close()
            }
        }.build()

        return _ArrowBack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowBack: ImageVector? = null
// Revue me>>
