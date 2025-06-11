package soft.divan.financemanager.presenter.uiKit


import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import soft.divan.financemanager.presenter.ui.screens.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    topBar: TopBar,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(topBar.title),) },
        actions = {
            if (topBar.actionIcon != null) {
                IconButton(onClick = { topBar.actionIconClick() }) {
                    Icon(topBar.actionIcon, contentDescription = "Add")
                }
            }
        },
        navigationIcon = {
            if (topBar.navigationIcon != null) {
                IconButton(onClick = { topBar.navigationIconClick() }) {
                    Icon(topBar.navigationIcon, contentDescription = "navigation")
                }
            }
        }
    )
}