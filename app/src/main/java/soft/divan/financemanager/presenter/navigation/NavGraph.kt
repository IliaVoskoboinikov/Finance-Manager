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
import soft.divan.financemanager.presenter.ui.screens.IncomeScreen
import soft.divan.financemanager.presenter.ui.screens.Screen
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
        composable(Screen.Expanses.route) { ExpensesScreen(modifier, navController) }
        composable(Screen.Income.route) { IncomeScreen(modifier, navController) }
        composable(Screen.Account.route) { AccountScreen(modifier, navController) }
        composable(Screen.Articles.route) { ArticlesScreen(modifier, navController) }
        composable(Screen.Settings.route) { SettingsScreen(modifier, navController) }
    }
}