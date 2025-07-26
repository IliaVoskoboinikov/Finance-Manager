import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import soft.divan.financemanager.presenter.navigation.ScreenBottom

@Composable
fun RowScope.FMNavigationBarItem(
    currentRoute: String?,
    screenBottom: ScreenBottom,
    navController: NavHostController
) {
    val destinationRoute = screenBottom.feature.route

    NavigationBarItem(
        colors = NavigationBarItemDefaults.colors()
            .copy(selectedIconColor = MaterialTheme.colorScheme.primary),
        selected = currentRoute == destinationRoute,
        onClick = {
            if (currentRoute != destinationRoute) {
                navController.navigate(destinationRoute) {
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
                fontWeight = if (currentRoute == destinationRoute) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
            )
        },
    )
}
