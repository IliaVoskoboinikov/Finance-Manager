package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Search: ImageVector
    get() {
        if (_Search != null) {
            return _Search!!
        }
        _Search = ImageVector.Builder(
            name = "Filled.Search",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(24f, 4f)
                    lineTo(24f, 4f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 24f)
                    lineTo(44f, 24f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 44f)
                    lineTo(24f, 44f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 24f)
                    lineTo(4f, 24f)
                    arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 4f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF49454F))) {
                    moveTo(31.6f, 33f)
                    lineTo(25.3f, 26.7f)
                    curveTo(24.8f, 27.1f, 24.225f, 27.417f, 23.575f, 27.65f)
                    curveTo(22.925f, 27.883f, 22.233f, 28f, 21.5f, 28f)
                    curveTo(19.683f, 28f, 18.146f, 27.371f, 16.888f, 26.112f)
                    curveTo(15.629f, 24.854f, 15f, 23.317f, 15f, 21.5f)
                    curveTo(15f, 19.683f, 15.629f, 18.146f, 16.888f, 16.888f)
                    curveTo(18.146f, 15.629f, 19.683f, 15f, 21.5f, 15f)
                    curveTo(23.317f, 15f, 24.854f, 15.629f, 26.112f, 16.888f)
                    curveTo(27.371f, 18.146f, 28f, 19.683f, 28f, 21.5f)
                    curveTo(28f, 22.233f, 27.883f, 22.925f, 27.65f, 23.575f)
                    curveTo(27.417f, 24.225f, 27.1f, 24.8f, 26.7f, 25.3f)
                    lineTo(33f, 31.6f)
                    lineTo(31.6f, 33f)
                    close()
                    moveTo(21.5f, 26f)
                    curveTo(22.75f, 26f, 23.813f, 25.563f, 24.688f, 24.688f)
                    curveTo(25.563f, 23.813f, 26f, 22.75f, 26f, 21.5f)
                    curveTo(26f, 20.25f, 25.563f, 19.188f, 24.688f, 18.313f)
                    curveTo(23.813f, 17.438f, 22.75f, 17f, 21.5f, 17f)
                    curveTo(20.25f, 17f, 19.188f, 17.438f, 18.313f, 18.313f)
                    curveTo(17.438f, 19.188f, 17f, 20.25f, 17f, 21.5f)
                    curveTo(17f, 22.75f, 17.438f, 23.813f, 18.313f, 24.688f)
                    curveTo(19.188f, 25.563f, 20.25f, 26f, 21.5f, 26f)
                    close()
                }
            }
        }.build()

        return _Search!!
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
