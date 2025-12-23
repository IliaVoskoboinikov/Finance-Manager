package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.screens

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
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.R
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.MyAccountsUiModel
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.MyAccountsUiState
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.mockMyAccountsUiStateSuccess
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.viewmodel.MyAccountsViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorContent
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
        MyAccounts(
            uiState = mockMyAccountsUiStateSuccess,
            loadAccounts = {},
            onNavigateToUpdateAccount = {},
            onNavigateToCreateAccount = {},
            hapticNavigation = {},
        )
    }
}

@Composable
fun MyAccountsScreen(
    modifier: Modifier = Modifier,
    onNavigateToUpdateAccount: (idAccount: String) -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    viewModel: MyAccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyAccounts(
        modifier = modifier,
        uiState = uiState,
        loadAccounts = viewModel::loadAccount,
        onNavigateToUpdateAccount = onNavigateToUpdateAccount,
        onNavigateToCreateAccount = onNavigateToCreateAccount,
        hapticNavigation = viewModel::hapticNavigation,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccounts(
    modifier: Modifier = Modifier,
    uiState: MyAccountsUiState,
    loadAccounts: () -> Unit,
    onNavigateToUpdateAccount: (idAccount: String) -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    hapticNavigation: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { AccountTopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingButton(onClick = {
                onNavigateToCreateAccount()
                hapticNavigation()
            })
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            MyAccountsStatefulContent(
                uiState = uiState,
                loadAccounts = loadAccounts,
                onNavigateToUpdateAccount = onNavigateToUpdateAccount,
            )
        }
    }
}

@Composable
private fun AccountTopBar() {
    TopBar(topBar = TopBarModel(title = R.string.my_accounts))
}

@Composable
private fun MyAccountsStatefulContent(
    uiState: MyAccountsUiState,
    loadAccounts: () -> Unit,
    onNavigateToUpdateAccount: (String) -> Unit
) {
    when (uiState) {
        is MyAccountsUiState.Loading -> LoadingProgressBar()
        is MyAccountsUiState.Error -> ErrorContent(onClick = {}) // TODO
        is MyAccountsUiState.Success -> AccountsSuccessContent(
            accounts = uiState.accounts,
            onNavigateToUpdateAccount = onNavigateToUpdateAccount
        )

        is MyAccountsUiState.EmptyData -> ErrorContent(
            messageResId = R.string.empty_data,
            onClick = { loadAccounts() })
    }
}

@Composable
fun AccountsSuccessContent(
    accounts: List<MyAccountsUiModel>,
    onNavigateToUpdateAccount: (idAccount: String) -> Unit
) {
    LazyColumn {
        items(
            items = accounts,
            key = { it.id }
        ) { account ->
            ItemAccount(onNavigateToUpdateAccount = onNavigateToUpdateAccount, account = account)
        }
    }
}

@Composable
private fun ItemAccount(
    onNavigateToUpdateAccount: (String) -> Unit,
    account: MyAccountsUiModel
) {
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
