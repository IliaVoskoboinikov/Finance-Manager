package soft.divan.financemanager.feature.expenses.expenses_impl.presenter.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.feature.expenses.expenses_impl.R
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.mapper.ExpensesUiState
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.viewmodel.ExpensesViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorSnackbar
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.FloatingButton
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.SubContentTextListItem
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.Clock
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ExpensesScreenPreview() {
    FinanceManagerTheme {
/*
        ExpensesScreen(navController = rememberNavController())
*/
    }
}

@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    onNavigateToHistory: () -> Unit,
    onNavigateToNewTransaction: () -> Unit,
    onNavigateToOldTransaction: (Int) -> Unit,
    navController: NavController,
    viewModel: ExpensesViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadTodayExpenses()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                topBar = TopBarModel(
                    title = R.string.expenses_today,
                    actionIcon = Icons.Filled.Clock,
                    actionIconClick = { onNavigateToHistory() }
                )
            )

            when (uiState) {
                is ExpensesUiState.Loading -> {
                    LoadingProgressBar()
                }

                is ExpensesUiState.Error -> {

                    ErrorSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = (uiState as ExpensesUiState.Error).message,
                    )
                }

                is ExpensesUiState.Success -> {
                    val state = (uiState as ExpensesUiState.Success)
                    Column() {
                        ListItem(
                            modifier = Modifier.height(56.dp),
                            content = { ContentTextListItem(stringResource(R.string.all)) },

                            trail = { ContentTextListItem(state.sumTransaction) },
                            containerColor = colorScheme.secondaryContainer
                        )
                        FMDriver()

                        val items = state.transactions
                        LazyColumn() {
                            items(items) { item ->
                                ListItem(
                                    modifier = Modifier
                                        .height(70.dp)
                                        .clickable {
                                            onNavigateToOldTransaction(item.id)
                                        },
                                    lead = {
                                        EmojiCircle(emoji = item.category.emoji)

                                    },
                                    content = {
                                        Column {
                                            ContentTextListItem(item.category.name)
                                            if (!item.comment.isNullOrEmpty()) {
                                                SubContentTextListItem(item.comment)
                                            }
                                        }
                                    },
                                    trail = {

                                        ContentTextListItem(item.amountFormatted)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Arrow,
                                            contentDescription = "arrow",
                                            tint = colorScheme.onSurfaceVariant

                                        )
                                    },

                                    )
                                FMDriver()
                            }
                        }
                    }
                }
            }
        }
        FloatingButton(
            onClick = { onNavigateToNewTransaction() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }
}
