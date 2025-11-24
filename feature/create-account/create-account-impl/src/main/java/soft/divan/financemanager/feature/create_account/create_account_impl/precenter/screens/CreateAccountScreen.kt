package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.create_account.create_account_impl.R
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountEvent
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountUiState
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.mockCreateAccountUiStateLoading
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.viewModel.CreateAccountViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowConfirm
import soft.divan.financemanager.uikit.icons.Cross
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CreateAccountScreenPreview() {
    FinanceManagerTheme {
        CreateAccountContent(
            uiState = mockCreateAccountUiStateLoading,
            onNavigateBack = { },
            onSave = { },
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Composable
fun CreateAccountScreenScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: CreateAccountViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CreateAccountEvent.Saved -> onNavigateBack()
                is CreateAccountEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = context.getString(event.messageRes),
                    withDismissAction = true
                )
            }
        }
    }

    CreateAccountContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSave = viewModel::createAccount,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun CreateAccountContent(
    modifier: Modifier = Modifier,
    uiState: CreateAccountUiState,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.create_account,
                    navigationIcon = Icons.Filled.Cross,
                    navigationIconClick = onNavigateBack,
                    actionIcon = Icons.Filled.ArrowConfirm,
                    actionIconClick = onSave
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is CreateAccountUiState.Error -> ErrorContent(onRetry = onSave)
                is CreateAccountUiState.Loading -> LoadingProgressBar()
                is CreateAccountUiState.Success -> CreateAccountForm(
                    uiState = uiState,
                )
            }
        }
    }
}

@Composable
fun CreateAccountForm(
    uiState: CreateAccountUiState.Success,
) {
    /* ListItem(
         modifier = Modifier
             .height(70.dp)
             .fillMaxWidth()
             .clickable {  },
         content = { ContentTextListItem(stringResource(R.string.data)) },
         trail = { ContentTextListItem() },
     )*/
}
