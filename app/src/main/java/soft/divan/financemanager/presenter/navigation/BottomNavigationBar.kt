package soft.divan.financemanager.presenter.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import soft.divan.financemanager.presenter.ui.screens.Screen


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Screen.items.forEach { screen ->
            FMNavigationBarItem(currentRoute, screen, navController)
        }
    }
}