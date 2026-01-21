package soft.divan.financemanager.feature.settings.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.design_app.api.DesignAppFeatureApi
import soft.divan.financemanager.feature.haptics.api.HapticsFeatureApi
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.impl.presenter.screens.AboutTheProgramScreen
import soft.divan.financemanager.feature.settings.impl.presenter.screens.SettingsScreen
import soft.divan.financemanager.feature.sounds.api.SoundsFeatureApi
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
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
    lateinit var hapticsFeatureApi: HapticsFeatureApi

    @Inject
    lateinit var soundsFeatureApi: SoundsFeatureApi

    @Inject
    lateinit var languagesFeatureApi: LanguagesFeatureApi

    @Inject
    lateinit var synchronizationFeatureApi: SynchronizationFeatureApi

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
                onNavigateToHaptic = { navController.navigate(hapticsFeatureApi.route) },
                onNavigateToSounds = { navController.navigate(soundsFeatureApi.route) },
                onNavigateToLanguages = { navController.navigate(languagesFeatureApi.route) },
                onNavigateToSynchronization = { navController.navigate(synchronizationFeatureApi.route) }
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