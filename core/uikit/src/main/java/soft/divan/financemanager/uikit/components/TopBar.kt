package soft.divan.financemanager.uikit.components


import androidx.compose.material.icons.Icons
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.core.uikit.R
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TopBarPreview() {
    FinanceManagerTheme {
        TopBar(
            topBar = TopBarModel(
                title = R.string.save,
                navigationIcon = Icons.Filled.ArrowBack,
                navigationIconClick = { },
                actionIcon = Icons.Filled.ArrowBack,
                actionIconClick = {}
            ))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    topBar: TopBarModel,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors()
            .copy(containerColor = MaterialTheme.colorScheme.primary),
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
                    Icon(topBar.navigationIcon, contentDescription = "Back")
                }
            }
        }
    )
}


