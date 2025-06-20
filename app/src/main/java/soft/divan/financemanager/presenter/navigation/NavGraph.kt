package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import soft.divan.financemanager.presenter.ui.screens.AccountScreen
import soft.divan.financemanager.presenter.ui.screens.ArticlesScreen
import soft.divan.financemanager.presenter.ui.screens.ExpensesScreen
import soft.divan.financemanager.presenter.ui.screens.HistoryScreen
import soft.divan.financemanager.presenter.ui.screens.IncomeScreen
import soft.divan.financemanager.presenter.ui.screens.SettingsScreen
import soft.divan.financemanager.presenter.ui.screens.SplashScreen


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = SplashScreen.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(SplashScreen.route) { SplashScreen(navController) }
        composable(ScreenBottom.ExpansesScreenBottom.route) { ExpensesScreen(modifier, navController) }
        composable(ScreenBottom.IncomeScreenBottom.route) { IncomeScreen(modifier, navController) }
        composable(ScreenBottom.AccountScreenBottom.route) { AccountScreen(modifier, navController) }
        composable(ScreenBottom.ArticlesScreenBottom.route) { ArticlesScreen(modifier, navController) }
        composable(ScreenBottom.SettingsScreenBottom.route) { SettingsScreen(modifier, navController) }
        composable(HistoryScreen.route) { HistoryScreen(modifier, navController) }
    }
}