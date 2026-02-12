package soft.divan.financemanager.feature.settings.impl.presenter.model

data class SettingsActions(
    val onNavigateToAboutTheProgram: () -> Unit,
    val onNavigateToSecurity: () -> Unit,
    val onNavigateToDesignApp: () -> Unit,
    val onNavigateToHaptic: () -> Unit,
    val onNavigateToSounds: () -> Unit,
    val onNavigateToLanguages: () -> Unit,
    val onNavigateToSynchronization: () -> Unit
)
