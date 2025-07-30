package soft.divan.financemanager.feature.settings.settings_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi

import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.settings_impl.screens.AboutTheProgramScreen
import soft.divan.financemanager.feature.settings.settings_impl.screens.ColorSelectionScreen
import soft.divan.financemanager.feature.settings.settings_impl.screens.SettingsScreen
import javax.inject.Inject

private const val baseRoute = "settings"
private const val scenarioSettingsRoute = "${baseRoute}/scenario"
private const val screenSettingsColorRoute = "$scenarioSettingsRoute/color"
private const val screenSettingsAboutTheProgramScreenRoute = "$scenarioSettingsRoute/about"


class SettingsFeatureImpl @Inject constructor() : SettingsFeatureApi {

    override val route: String = baseRoute

    @Inject
    lateinit var securityFeatureApi: SecurityFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            SettingsScreen(
                modifier = modifier,
                navController = navController,
                onNavigateToColor = {
                    navController.navigate(screenSettingsColorRoute)
                },
                onNavigateToAboutTheProgram = {
                    navController.navigate(screenSettingsAboutTheProgramScreenRoute)
                },
                onNavigateToSecurity = {
                    navController.navigate(securityFeatureApi.route)
                }
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scenarioSettingsRoute,
            startDestination = screenSettingsColorRoute
        ) {

            composable(route = screenSettingsColorRoute) {
                ColorSelectionScreen(
                    modifier = modifier,
                    navController = navController
                )
            }

            composable(route = screenSettingsAboutTheProgramScreenRoute) {
                AboutTheProgramScreen(
                    modifier = modifier,
                    navController = navController
                )
            }

        }

    }
}