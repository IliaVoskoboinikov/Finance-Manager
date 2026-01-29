import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import soft.divan.financemanager.presenter.navigation.ScreenBottom

@Composable
fun RowScope.FmNavigationBarItem(
    navController: NavController,
    screenBottom: ScreenBottom,
    hapticToggleMenu: () -> Unit
) {

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val selected = currentDestination
        ?.hierarchy
        ?.any { it.route?.startsWith(screenBottom.route) == true }
        ?: false

    NavigationBarItem(
        selected = selected,
        onClick = {
            hapticToggleMenu()
            if (!selected) {
                navController.navigate(screenBottom.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        icon = {
            Icon(
                screenBottom.icon,
                contentDescription = stringResource(screenBottom.title),
                modifier = Modifier.size(32.dp)
            )
        },
        label = {
            Text(
                text = stringResource(screenBottom.title),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
            )
        },
    )
}
