package soft.divan.financemanager.feature.settings.settings_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.haptic.haptic_api.HapticFeatureApi
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi

import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.settings_impl.presenter.screens.AboutTheProgramScreen
import soft.divan.financemanager.feature.settings.settings_impl.presenter.screens.SettingsScreen
import soft.divan.financemanager.feature.sounds.sounds_api.SoundsFeatureApi
import javax.inject.Inject

private const val BASE_ROUTE = "settings"
private const val scenarioSettingsRoute = "${BASE_ROUTE}/scenario"

private const val screenSettingsAboutTheProgramScreenRoute = "$scenarioSettingsRoute/about"


class SettingsFeatureImpl @Inject constructor() : SettingsFeatureApi {

    override val route: String = BASE_ROUTE

    @Inject
    lateinit var securityFeatureApi: SecurityFeatureApi

    @Inject
    lateinit var designAppFeatureApi: DesignAppFeatureApi

    @Inject
    lateinit var hapticFeatureApi: HapticFeatureApi

    @Inject
    lateinit var soundsFeatureApi: SoundsFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            SettingsScreen(
                modifier = modifier,
                onNavigateToAboutTheProgram = {
                    navController.navigate(screenSettingsAboutTheProgramScreenRoute)
                },
                onNavigateToSecurity = { navController.navigate(securityFeatureApi.route) },
                onNavigateToDesignApp = { navController.navigate(designAppFeatureApi.route) },
                onNavigateToHaptic = { navController.navigate(hapticFeatureApi.route) },
                onNavigateToSounds = { navController.navigate(soundsFeatureApi.route) }
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scenarioSettingsRoute,
            startDestination = screenSettingsAboutTheProgramScreenRoute
        ) {


            composable(route = screenSettingsAboutTheProgramScreenRoute) {
                AboutTheProgramScreen(
                    modifier = modifier,
                )
            }

        }

    }
}