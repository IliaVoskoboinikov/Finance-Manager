package soft.divan.financemanager.uikit.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TopBarModelTest {

    @Test
    fun `defaults have no icons`() {
        val model = TopBarModel(title = 42)

        assertThat(model.title).isEqualTo(42)
        assertThat(model.navigationIcon).isNull()
        assertThat(model.actionIcon).isNull()
    }

    @Test
    fun `default click handlers are no-op and do not throw`() {
        val model = TopBarModel(title = 1)

        model.navigationIconClick()
        model.actionIconClick()
    }

    @Test
    fun `stores provided icons and invokes click handlers`() {
        var navClicks = 0
        var actionClicks = 0
        val icon = Icons.Default.Add

        val model = TopBarModel(
            title = 7,
            navigationIcon = icon,
            navigationIconClick = { navClicks++ },
            actionIcon = icon,
            actionIconClick = { actionClicks++ }
        )

        model.navigationIconClick()
        model.actionIconClick()
        model.actionIconClick()

        assertThat(model.navigationIcon).isSameAs(icon)
        assertThat(model.actionIcon).isSameAs(icon)
        assertThat(navClicks).isEqualTo(1)
        assertThat(actionClicks).isEqualTo(2)
    }

    @Test
    fun `copy overrides only the requested field`() {
        val original = TopBarModel(title = 1)

        val updated = original.copy(title = 2)

        assertThat(updated.title).isEqualTo(2)
        assertThat(updated.navigationIcon).isNull()
    }
}
