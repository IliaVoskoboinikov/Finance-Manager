package soft.divan.financemanager.feature.settings.settings_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.settings_impl.screens.SettingsScreen
import javax.inject.Inject

class SettingsFeatureImpl @Inject constructor() : SettingsFeatureApi {

    override val settingsRoute: String = "settings"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(settingsRoute) {
            SettingsScreen(
                modifier = modifier,
                navController = navController
            )
        }
    }
}