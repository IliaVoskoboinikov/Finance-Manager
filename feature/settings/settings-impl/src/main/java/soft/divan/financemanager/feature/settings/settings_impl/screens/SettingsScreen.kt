package soft.divan.financemanager.feature.settings.settings_impl.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import soft.divan.financemanager.feature.settings.settings_impl.R
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Triangle
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SettingsScreenPreview() {
    FinanceManagerTheme(true) {
        /*ettingsScreen(navController = rememberNavController())*/
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToColor: () -> Unit,
    onNavigateToAboutTheProgram: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToDesignApp: () -> Unit,
) {


    val settingsItems = listOf(

        SettingsListItemModel.WithArrow(
            title = R.string.design,
            onClick = {
                onNavigateToDesignApp()
            }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.sounds,
            onClick = { /* handle */ }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.haptics,
            onClick = { /* handle */ }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.passcode,
            onClick = { onNavigateToSecurity() }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.synchronization,
            onClick = { /* handle */ }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.language,
            onClick = { /* handle */ }
        ),
        SettingsListItemModel.WithArrow(
            title = R.string.program_notes,
            onClick = { onNavigateToAboutTheProgram() }
        )

    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                topBar = TopBarModel(title = R.string.settings),
            )
            LazyColumn {
                items(settingsItems) { item ->
                    RenderSettingsListItem(item)
                    FMDriver()
                }
            }
        }

    }


}

sealed interface SettingsListItemModel {
    data class WithSwitch(
        val title: Int,
        val isChecked: Boolean,
        val onToggle: (Boolean) -> Unit
    ) : SettingsListItemModel

    data class WithArrow(
        val title: Int,
        val onClick: () -> Unit
    ) : SettingsListItemModel
}


@Composable
fun RenderSettingsListItem(model: SettingsListItemModel) {
    when (model) {
        is SettingsListItemModel.WithSwitch -> {
            ListItem(
                modifier = Modifier.height(56.dp),
                content = { ContentTextListItem(stringResource(model.title)) },
                trail = {
                    Switch(
                        checked = model.isChecked,
                        onCheckedChange = model.onToggle
                    )
                },
            )
        }

        is SettingsListItemModel.WithArrow -> {
            ListItem(
                modifier = Modifier
                    .height(55.dp)
                    .clickable(onClick = model.onClick),
                content = {
                    ContentTextListItem(
                        stringResource(model.title)
                    )
                },
                trail = {
                    Icon(
                        imageVector = Icons.Filled.Triangle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant

                    )
                },
            )
        }
    }
}

