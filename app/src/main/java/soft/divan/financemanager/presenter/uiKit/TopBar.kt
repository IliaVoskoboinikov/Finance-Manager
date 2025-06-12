package soft.divan.financemanager.presenter.uiKit


import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import soft.divan.financemanager.presenter.ui.screens.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    topBar: TopBar,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {

            Text(
                text = stringResource(topBar.title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight,
                    letterSpacing = MaterialTheme.typography.titleLarge.letterSpacing,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = modifier
            )

        },
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