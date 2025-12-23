package soft.divan.financemanager.presenter.navigation

import FMNavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(
    modifier: Modifier,
    navController: NavHostController,
    screens: List<ScreenBottom>,
    currentRoute: String?,
    hapticToggleMenu: () -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        screens.forEach { screen ->
            FMNavigationBarItem(currentRoute, screen, navController, hapticToggleMenu)
        }
    }
}
