package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.model.HistoryUiItemList
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.HistoryViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.EmojiCircle
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar
import soft.divan.financemanager.presenter.uiKit.SubContentTextListItem


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HistoryScreenPreview() {
    FinanceManagerTheme {
        HistoryContent(uiState = provideMockHistoryUiState())
    }
}


object HistoryScreen {
    const val route = "history"
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(modifier = modifier, uiState = uiState)
}

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {


    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                LoadingProgressBar()
            }

            is HistoryUiState.Error -> {
                ErrorSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = uiState.message,
                )
            }

            is HistoryUiState.Success -> {
                val items = uiState.items
                Column(modifier = Modifier.padding(innerPadding)) {
                    LazyColumn {
                        items(items) { item ->
                            RenderHistoryListItem(item)
                            FMDriver()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderHistoryListItem(item: HistoryUiItemList) {
    when (item) {
        is HistoryUiItemList.DateAndBalance -> {
            ListItem(
                modifier = Modifier.height(56.dp),
                content = { ContentTextListItem(stringResource(item.content)) },
                trail = { ContentTextListItem(item.trail) },
                containerColor = colorScheme.secondaryContainer
            )
        }

        is HistoryUiItemList.Item -> {
            ListItem(
                modifier = Modifier.height(70.dp),
                lead = {
                    EmojiCircle(emoji = item.emoji)

                },
                content = {
                    Column {
                        ContentTextListItem(item.content)
                        if (item.subContent.isNotEmpty()) {
                            SubContentTextListItem(item.subContent)
                        }
                    }
                },
                trail = {
                    Column {
                        ContentTextListItem(item.price)
                        ContentTextListItem(item.time)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
            )
        }
    }
}

fun provideMockHistoryUiState(): HistoryUiState.Success {
    return HistoryUiState.Success(
        listOf(
            HistoryUiItemList.DateAndBalance(content = R.string.all, trail = "Февраль 2025"),
            HistoryUiItemList.DateAndBalance(content = R.string.start, trail = "Февраль 2025"),
            HistoryUiItemList.DateAndBalance(content = R.string.end, trail = "125 868 ₽"),
            HistoryUiItemList.Item(
                emoji = "pk",
                content = "Ремонт квартиры",
                subContent = "Фурнитура для дверей",
                price = "58 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                price = "100 000 ₽",
                subContent = "",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
        )
    )
}
