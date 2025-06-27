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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.model.ExpensesUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.ExpensesViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.EmojiCircle
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar
import soft.divan.financemanager.presenter.uiKit.SubContentTextListItem

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ExpensesScreenPreview() {
    FinanceManagerTheme {
        ExpensesScreen(navController = rememberNavController())
    }
}

@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ExpensesViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingButton(onClick = {})
        }
    ) { innerPadding ->
        when (uiState) {
            is ExpensesUiState.Loading -> {                LoadingProgressBar()
            }
            is ExpensesUiState.Error -> { ErrorSnackbar(
                snackbarHostState = snackbarHostState,
                message = (uiState as ExpensesUiState.Error).message,
            )
            }

            is ExpensesUiState.Success -> {
                val state = (uiState as ExpensesUiState.Success)
                Column(modifier = Modifier.padding(innerPadding)) {
                    ListItem(
                        modifier = Modifier.height(56.dp),
                        content = { ContentTextListItem(stringResource(R.string.all)) },
                        trail = { ContentTextListItem(state.sumTransaction + " ₽")  },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()

                    val items = state.transactions
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(items) { item ->
                            ListItem(
                                modifier = Modifier.height(70.dp),
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

                                    ContentTextListItem(item.amount.toPlainString())
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
}
