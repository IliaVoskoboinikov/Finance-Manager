package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.icons.Triangle
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme




@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SettingsScreenPreview() {
    FinanceManagerTheme {
        SettingsScreen(navController = rememberNavController())
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(settingsItems) { item ->
                RenderSettingsListItem(item)
                FMDriver()
            }
        }
    }
}

private val settingsItems = listOf(
    SettingsListItemModel.WithSwitch(
        title = R.string.dark_theme,
        isChecked = false,
        onToggle = { isChecked -> /* handle */ }
    ),
    SettingsListItemModel.WithArrow(
        title = R.string.primary_color,
        onClick = { /* handle */ }
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
        onClick = { /* handle */ }
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
        onClick = { /* handle */ }
    )

)

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
                modifier = Modifier.height(55.dp),
                content = { ContentTextListItem(stringResource(model.title)) },
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

