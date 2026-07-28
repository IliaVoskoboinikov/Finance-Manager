package soft.divan.financemanager.uikit.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Дымовые тесты кастомных иконок: каждая иконка должна строиться без исключений,
 * кэшироваться (один и тот же инстанс при повторном обращении) и иметь непустой контент.
 */
class IconsTest {

    private val icons: Map<String, ImageVector> = mapOf(
        "AddRound" to Icons.Filled.AddRound,
        "Arrow" to Icons.Filled.Arrow,
        "ArrowBack" to Icons.Filled.ArrowBack,
        "ArrowConfirm" to Icons.Filled.ArrowConfirm,
        "Calculator" to Icons.Filled.Calculator,
        "Chart90" to Icons.Filled.Chart90,
        "Clock" to Icons.Filled.Clock,
        "Cross" to Icons.Filled.Cross,
        "Diagram" to Icons.Filled.Diagram,
        "Downtrend" to Icons.Filled.Downtrend,
        "Dress" to Icons.Filled.Dress,
        "Home" to Icons.Filled.Home,
        "MdiDollar" to Icons.Filled.MdiDollar,
        "MdiEuro" to Icons.Filled.MdiEuro,
        "MdiRuble" to Icons.Filled.MdiRuble,
        "Pencil" to Icons.Filled.Pencil,
        "RoundCross" to Icons.Filled.RoundCross,
        "Search" to Icons.Filled.Search,
        "Settings" to Icons.Filled.Settings,
        "TabletWatch" to Icons.Filled.TabletWatch,
        "Triangle" to Icons.Filled.Triangle,
        "Uptrend" to Icons.Filled.Uptrend
    )

    @Test
    fun `every icon builds successfully with vector content`() {
        icons.forEach { (name, icon) ->
            assertThat(icon.root.size)
                .withFailMessage("Icon %s has empty vector content", name)
                .isGreaterThan(0)
        }
    }

    @Test
    fun `every icon has positive dimensions`() {
        icons.forEach { (name, icon) ->
            assertThat(icon.viewportWidth)
                .withFailMessage("Icon %s has invalid viewport width", name)
                .isGreaterThan(0f)
            assertThat(icon.viewportHeight)
                .withFailMessage("Icon %s has invalid viewport height", name)
                .isGreaterThan(0f)
        }
    }

    @Test
    fun `icons are cached and return the same instance`() {
        assertThat(Icons.Filled.AddRound).isSameAs(Icons.Filled.AddRound)
        assertThat(Icons.Filled.Search).isSameAs(Icons.Filled.Search)
        assertThat(Icons.Filled.Home).isSameAs(Icons.Filled.Home)
    }
}
