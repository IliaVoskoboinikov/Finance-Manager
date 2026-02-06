package soft.divan.financemanager.feature.transactionstoday.impl.presenter.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.transactions_today.impl.R
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.TransactionUi
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.TransactionsTodayUiState
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.mockTransactionsTodayUiStateSuccess
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.viewmodel.TransactionsTodayViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorContent
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
fun TransactionsTodayPreview() {
    FinanceManagerTheme {
        TransactionsTodayContent(
            isIncome = false,
            uiState = mockTransactionsTodayUiStateSuccess,
            onRetry = {},
            onNavigateToHistory = {},
            onNavigateToNewTransaction = {},
            onNavigateToOldTransaction = {},
            hapticNavigation = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Composable
fun TransactionsTodayScreen(
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    onNavigateToHistory: () -> Unit,
    onNavigateToNewTransaction: () -> Unit,
    onNavigateToOldTransaction: (idTransaction: String) -> Unit,
    viewModel: TransactionsTodayViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isIncome) {
        viewModel.loadTodayTransactions(isIncome)
    }

    TransactionsTodayContent(
        modifier = modifier,
        isIncome = isIncome,
        uiState = uiState,
        onRetry = viewModel::retry,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToNewTransaction = onNavigateToNewTransaction,
        onNavigateToOldTransaction = onNavigateToOldTransaction,
        hapticNavigation = viewModel::hapticNavigation,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun TransactionsTodayContent(
    modifier: Modifier = Modifier,
    isIncome: Boolean,
    uiState: TransactionsTodayUiState,
    onRetry: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNewTransaction: () -> Unit,
    hapticNavigation: () -> Unit,
    onNavigateToOldTransaction: (idTransaction: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = if (isIncome) R.string.income_today else R.string.expenses_today,
                    actionIcon = Icons.Filled.Clock,
                    actionIconClick = { onNavigateToHistory() }
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingButton(onClick = {
                onNavigateToNewTransaction()
                hapticNavigation()
            })
        }
    ) { paddingValues ->

        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is TransactionsTodayUiState.Loading -> LoadingProgressBar()

                is TransactionsTodayUiState.Error -> ErrorContent(onClick = { onRetry() })

                is TransactionsTodayUiState.Success -> TransactionsList(
                    modifier = modifier,
                    uiState = uiState,
                    onNavigateToOldTransaction = onNavigateToOldTransaction
                )
            }
        }
    }
}

@Composable
fun TransactionsList(
    modifier: Modifier = Modifier,
    uiState: TransactionsTodayUiState.Success,
    onNavigateToOldTransaction: (idTransaction: String) -> Unit
) {
    Column(modifier = modifier) {
        SummaryItem(sum = uiState.sumTransaction)

        FMDriver()

        LazyColumn {
            items(
                items = uiState.transactions,
                key = { it.id }
            ) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onClick = { onNavigateToOldTransaction(transaction.id) }
                )
                FMDriver()
            }
        }
    }
}

@Composable
private fun SummaryItem(sum: String) {
    ListItem(
        modifier = Modifier.height(56.dp),
        content = { ContentTextListItem(stringResource(R.string.all)) },
        trail = { ContentTextListItem(sum) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    )
}

@Composable
private fun TransactionListItem(
    transaction: TransactionUi,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .clickable(onClick = onClick),
        lead = { EmojiCircle(transaction.category.emoji) },
        content = {
            Column {
                ContentTextListItem(transaction.category.name)
                if (transaction.comment.isNotEmpty()) {
                    SubContentTextListItem(transaction.comment)
                }
            }
        },
        trail = {
            ContentTextListItem(transaction.amountFormatted)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Arrow,
                contentDescription = "arrow",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
// Revue me>>
