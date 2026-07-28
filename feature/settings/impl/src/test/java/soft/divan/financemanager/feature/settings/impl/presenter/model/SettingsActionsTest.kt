package soft.divan.financemanager.feature.settings.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SettingsActionsTest {

    @Test
    fun `actions invoke provided navigation handlers`() {
        var calls = 0
        val actions = SettingsActions(
            onNavigateToAboutTheProgram = { calls++ },
            onNavigateToSecurity = { calls++ },
            onNavigateToDesignApp = { calls++ },
            onNavigateToHaptic = { calls++ },
            onNavigateToSounds = { calls++ },
            onNavigateToLanguages = { calls++ },
            onNavigateToSynchronization = { calls++ },
            onNavigateToProfile = { calls++ }
        )

        actions.onNavigateToAboutTheProgram()
        actions.onNavigateToSecurity()
        actions.onNavigateToDesignApp()
        actions.onNavigateToHaptic()
        actions.onNavigateToSounds()
        actions.onNavigateToLanguages()
        actions.onNavigateToSynchronization()
        actions.onNavigateToProfile()

        assertThat(calls).isEqualTo(8)
    }
}
