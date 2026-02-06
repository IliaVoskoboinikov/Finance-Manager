package soft.divan.financemanager.feature.settings.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.designapp.api.DesignAppFeatureApi
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
private const val SCENARIO_SETTINGS_ROUTE = "${BASE_ROUTE}/scenario"
private const val SCREEN_SETTINGS_ABOUT_THE_PROGRAM_SCREEN_ROUTE = "$SCENARIO_SETTINGS_ROUTE/about"

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
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            SettingsScreen(
                modifier = modifier,
                onNavigateToAboutTheProgram = {
                    navController.navigate(
                        scope.route(SCREEN_SETTINGS_ABOUT_THE_PROGRAM_SCREEN_ROUTE)
                    )
                },
                onNavigateToSecurity = {
                    navController.navigate(
                        scope.route(securityFeatureApi.route)
                    )
                },
                onNavigateToDesignApp = {
                    navController.navigate(
                        scope.route(designAppFeatureApi.route)
                    )
                },
                onNavigateToHaptic = {
                    navController.navigate(
                        scope.route(hapticsFeatureApi.route)
                    )
                },
                onNavigateToSounds = {
                    navController.navigate(
                        scope.route(soundsFeatureApi.route)
                    )
                },
                onNavigateToLanguages = {
                    navController.navigate(
                        scope.route(languagesFeatureApi.route)
                    )
                },
                onNavigateToSynchronization = {
                    navController.navigate(
                        scope.route(
                            synchronizationFeatureApi.route
                        )
                    )
                }
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scope.route(SCENARIO_SETTINGS_ROUTE),
            startDestination = scope.route(SCREEN_SETTINGS_ABOUT_THE_PROGRAM_SCREEN_ROUTE)
        ) {
            composable(route = scope.route(SCREEN_SETTINGS_ABOUT_THE_PROGRAM_SCREEN_ROUTE)) {
                AboutTheProgramScreen(
                    modifier = modifier
                )
            }
        }

        registerChildFeatures(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope,
            modifier = modifier
        )
    }

    private fun registerChildFeatures(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        designAppFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(designAppFeatureApi.route),
            modifier
        )

        soundsFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(soundsFeatureApi.route),
            modifier
        )

        hapticsFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(hapticsFeatureApi.route),
            modifier
        )

        securityFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(securityFeatureApi.route),
            modifier
        )

        synchronizationFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(synchronizationFeatureApi.route),
            modifier
        )

        languagesFeatureApi.registerGraph(
            navGraphBuilder,
            navController,
            scope.child(languagesFeatureApi.route),
            modifier
        )
    }
}
// Revue me>>
