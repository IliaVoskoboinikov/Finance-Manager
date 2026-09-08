package soft.divan.financemanager.feature.settings.impl.presenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavEdge
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.feature.auth.api.ProfileKey
import soft.divan.financemanager.feature.designapp.api.DesignAppKey
import soft.divan.financemanager.feature.haptics.api.HapticsKey
import soft.divan.financemanager.feature.languages.api.LanguagesKey
import soft.divan.financemanager.feature.security.api.SecurityKey
import soft.divan.financemanager.feature.settings.api.AboutTheProgramKey
import soft.divan.financemanager.feature.settings.api.SettingsKey
import soft.divan.financemanager.feature.settings.impl.R
import soft.divan.financemanager.feature.settings.impl.presenter.model.SettingsModel
import soft.divan.financemanager.feature.settings.impl.presenter.model.SettingsActions
import soft.divan.financemanager.feature.sounds.api.SoundsKey
import soft.divan.financemanager.feature.synchronization.api.SynchronizationKey
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Triangle
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@NavPreview(route = SettingsKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SettingsScreenPreview() {
    FinanceManagerTheme {
        SettingsScreen(
            actions = SettingsActions(
                onNavigateToAboutTheProgram = {},
                onNavigateToSecurity = {},
                onNavigateToDesignApp = {},
                onNavigateToHaptic = {},
                onNavigateToSounds = {},
                onNavigateToLanguages = {},
                onNavigateToSynchronization = {},
                onNavigateToProfile = {}
            )
        )
    }
}

@NavDestination(route = SettingsKey::class)
@NavEdge(to = AboutTheProgramKey::class, label = "о программе")
@NavEdge(to = SecurityKey::class, label = "безопасность")
@NavEdge(to = DesignAppKey::class, label = "оформление")
@NavEdge(to = HapticsKey::class, label = "хаптик")
@NavEdge(to = SoundsKey::class, label = "звуки")
@NavEdge(to = LanguagesKey::class, label = "язык")
@NavEdge(to = SynchronizationKey::class, label = "синхронизация")
@NavEdge(to = ProfileKey::class, label = "профиль")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    actions: SettingsActions
) {
    val settingsItems = remember(actions) {
        provideSettings(actions)
    }

    Scaffold(
        topBar = { TopBar(topBar = TopBarModel(title = R.string.settings)) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            settingsItems.forEach { item ->
                SettingsItem(model = item)
            }
        }
    }
}

@Composable
private fun SettingsItem(model: SettingsModel) {
    ListItem(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable(onClick = model.onClick),
        content = {
            ContentTextListItem(stringResource(model.title))
        },
        trail = {
            Icon(
                imageVector = Icons.Filled.Triangle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
    FMDriver()
}

private fun provideSettings(
    navigation: SettingsActions
): List<SettingsModel> {
    return listOf(
        SettingsModel(R.string.profile) { navigation.onNavigateToProfile() },
        SettingsModel(R.string.design) { navigation.onNavigateToDesignApp() },
        SettingsModel(R.string.sounds) { navigation.onNavigateToSounds() },
        SettingsModel(R.string.haptics) { navigation.onNavigateToHaptic() },
        SettingsModel(R.string.passcode) { navigation.onNavigateToSecurity() },
        SettingsModel(R.string.synchronization) { navigation.onNavigateToSynchronization() },
        SettingsModel(R.string.language) { navigation.onNavigateToLanguages() },
        SettingsModel(R.string.program_notes) { navigation.onNavigateToAboutTheProgram() }
    )
}
