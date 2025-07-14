package soft.divan.financemanager.presenter.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController


@Composable
fun BottomNavigationBar(
    modifier: Modifier,
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        ScreenBottom.items.forEach { screen ->
            FMNavigationBarItem(currentRoute, screen, navController)
        }
    }
}