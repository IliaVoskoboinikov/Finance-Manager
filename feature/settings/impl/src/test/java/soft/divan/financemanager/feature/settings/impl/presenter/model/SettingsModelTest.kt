package soft.divan.financemanager.feature.settings.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SettingsModelTest {

    @Test
    fun `model exposes title and invokes click handler`() {
        var clicks = 0
        val model = SettingsModel(title = 42, onClick = { clicks++ })

        model.onClick()
        model.onClick()

        assertThat(model.title).isEqualTo(42)
        assertThat(clicks).isEqualTo(2)
    }

    @Test
    fun `equality is structural for the same handler`() {
        val handler = {}

        assertThat(SettingsModel(1, handler)).isEqualTo(SettingsModel(1, handler))
        assertThat(SettingsModel(1, handler)).isNotEqualTo(SettingsModel(2, handler))
    }
}
