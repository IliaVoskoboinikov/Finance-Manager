package soft.divan.financemanager.presenter.navigation

import FmNavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBar(
    modifier: Modifier,
    backStack: TopLevelBackStack,
    screens: List<ScreenBottom>,
    hapticToggleMenu: () -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        screens.forEach { screen ->
            FmNavigationBarItem(backStack, screen, hapticToggleMenu)
        }
    }
}
