package soft.divan.financemanager.presenter.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import soft.divan.financemanager.presenter.ui.screens.Screen

@Composable
fun RowScope.FMNavigationBarItem(
    currentRoute: String?,
    screen: Screen,
    navController: NavHostController
) {
    NavigationBarItem(
        selected = currentRoute == screen.route,
        onClick = {
            if (currentRoute != screen.route) {
                navController.navigate(screen.route) {
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
                screen.icon,
                contentDescription = stringResource(screen.title),
                modifier = Modifier.size(32.dp)
            )
        },
        label = {
            Text(
                text = stringResource(screen.title),
                fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
            )
        },
    )
}