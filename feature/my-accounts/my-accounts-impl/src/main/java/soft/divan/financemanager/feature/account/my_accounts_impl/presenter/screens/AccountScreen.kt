package soft.divan.financemanager.feature.account.my_accounts_impl.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.account.my_accounts_impl.presenter.model.AccountUiModel
import soft.divan.financemanager.feature.account.my_accounts_impl.presenter.model.AccountUiState
import soft.divan.financemanager.feature.account.my_accounts_impl.presenter.viewmodel.AccountViewModel
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.R
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorSnackbar
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.FloatingButton
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountScreenPreview() {
    FinanceManagerTheme {
        AccountContent(
            uiState = AccountUiState.Success(
                accounts = listOf(
                    AccountUiModel(
                        id = 1,
                        name = "основной счет",
                        balance = "1000000$",
                        currency = "$",
                    ), AccountUiModel(
                        id = 2,
                        name = "основной счет2",
                        balance = "1000000$",
                        currency = "$",
                    )
                )
            ),
            onNavigateToUpdateAccount = {},
            onNavigateToCreateAccount = {},
        )
    }
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onNavigateToUpdateAccount: (idAccount: Int) -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToUpdateAccount = onNavigateToUpdateAccount,
        onNavigateToCreateAccount = onNavigateToCreateAccount,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    onNavigateToUpdateAccount: (idAccount: Int) -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { TopBar(topBar = TopBarModel(title = R.string.my_accounts)) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { FloatingButton(onClick = onNavigateToCreateAccount) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is AccountUiState.Loading -> LoadingProgressBar()
                is AccountUiState.Error -> ErrorSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = uiState.message,
                )

                is AccountUiState.Success -> Accounts(
                    accounts = uiState.accounts,
                    onNavigateToUpdateAccount = onNavigateToUpdateAccount
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Accounts(
    accounts: List<AccountUiModel>,
    onNavigateToUpdateAccount: (idAccount: Int) -> Unit,
) {
    LazyColumn {
        items(accounts) { account ->
            ListItem(
                modifier = Modifier
                    .height(56.dp)
                    .clickable { onNavigateToUpdateAccount(account.id) },
                lead = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "\uD83D\uDCB0", textAlign = TextAlign.Center)
                    }
                },
                content = { ContentTextListItem(account.name) },
                trail = {
                    ContentTextListItem(account.balance)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                containerColor = colorScheme.secondaryContainer
            )
            FMDriver()
        }
    }
}
