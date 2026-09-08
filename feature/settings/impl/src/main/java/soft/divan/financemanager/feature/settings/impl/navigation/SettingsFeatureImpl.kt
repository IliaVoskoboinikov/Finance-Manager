package soft.divan.financemanager.feature.settings.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.auth.api.ProfileKey
import soft.divan.financemanager.feature.designapp.api.DesignAppKey
import soft.divan.financemanager.feature.haptics.api.HapticsKey
import soft.divan.financemanager.feature.languages.api.LanguagesKey
import soft.divan.financemanager.feature.security.api.SecurityKey
import soft.divan.financemanager.feature.settings.api.AboutTheProgramKey
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsKey
import soft.divan.financemanager.feature.settings.impl.presenter.model.SettingsActions
import soft.divan.financemanager.feature.settings.impl.presenter.screens.AboutTheProgramScreen
import soft.divan.financemanager.feature.settings.impl.presenter.screens.SettingsScreen
import soft.divan.financemanager.feature.sounds.api.SoundsKey
import soft.divan.financemanager.feature.synchronization.api.SynchronizationKey
import javax.inject.Inject

class SettingsFeatureImpl @Inject constructor() : SettingsFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<SettingsKey> {
            SettingsScreen(
                modifier = modifier,
                actions = SettingsActions(
                    onNavigateToAboutTheProgram = { navigator.goTo(AboutTheProgramKey) },
                    onNavigateToSecurity = { navigator.goTo(SecurityKey) },
                    onNavigateToDesignApp = { navigator.goTo(DesignAppKey) },
                    onNavigateToHaptic = { navigator.goTo(HapticsKey) },
                    onNavigateToSounds = { navigator.goTo(SoundsKey) },
                    onNavigateToLanguages = { navigator.goTo(LanguagesKey) },
                    onNavigateToSynchronization = { navigator.goTo(SynchronizationKey) },
                    onNavigateToProfile = { navigator.goTo(ProfileKey) }
                )
            )
        }

        scope.entry<AboutTheProgramKey> {
            AboutTheProgramScreen(
                modifier = modifier
            )
        }
    }
}
