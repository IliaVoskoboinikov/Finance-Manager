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
import soft.divan.financemanager.presenter.navigation.ScreenBottom
import soft.divan.financemanager.presenter.navigation.TopLevelBackStack

@Composable
fun RowScope.FmNavigationBarItem(
    backStack: TopLevelBackStack,
    screenBottom: ScreenBottom,
    hapticToggleMenu: () -> Unit
) {
    // Вкладка подсвечена по корневому ключу её стека, а не по текущему экрану: вложенные
    // экраны вкладки (история, операция, настройки) держат подсветку своей вкладки.
    val selected = backStack.currentTabKey == screenBottom.key

    NavigationBarItem(
        selected = selected,
        onClick = {
            hapticToggleMenu()
            if (!selected) {
                backStack.switchTab(screenBottom.key)
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
                fontSize = 12.sp
            )
        }
    )
}
