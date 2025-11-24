package soft.divan.financemanager.feature.account.account_impl.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.account.account_impl.R
import soft.divan.financemanager.feature.account.account_impl.presenter.model.AccountUiModel
import soft.divan.financemanager.feature.account.account_impl.presenter.model.AccountUiState
import soft.divan.financemanager.feature.account.account_impl.presenter.viewmodel.AccountViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorSnackbar
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.FloatingButton
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.Diagram
import soft.divan.financemanager.uikit.icons.Pencil
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountScreenPreview() {
    FinanceManagerTheme {
        AccountContent(
            uiState = AccountUiState.Success(
                account = AccountUiModel(
                    id = 1,
                    name = "основной счет",
                    balance = "1000000$",
                    currency = "$",
                )
            ),
            onNavigateToCreateAccount = {},
            updateName = {}
        )
    }
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onNavigateToCreateAccount: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToCreateAccount = onNavigateToCreateAccount,
        updateName = viewModel::updateName,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    onNavigateToCreateAccount: () -> Unit,
    updateName: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                topBar = TopBarModel(
                    title = R.string.my_accounts,
                    actionIcon = Icons.Filled.Pencil,
                    actionIconClick = { /*navController ->*/

                    }
                )
            )
            when (uiState) {
                is AccountUiState.Loading -> {
                    LoadingProgressBar()
                }

                is AccountUiState.Error -> {
                    ErrorSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = uiState.message,
                    )
                }

                is AccountUiState.Success -> {
                    UiStateSuccessContent(uiState, updateName)
                }
            }
        }
        FloatingButton(
            onClick = onNavigateToCreateAccount,
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UiStateSuccessContent(
    uiState: AccountUiState.Success,
    updateName: (String) -> Unit
) {
    val updateNameShowDialog = remember { mutableStateOf(false) }

    Column {
        AccountBalance(uiState, updateNameShowDialog)
        FMDriver()
        Spacer(modifier = Modifier.height(16.dp))
        Diagram()
        UpdateName(updateNameShowDialog, updateName)
    }
}

@Composable
private fun AccountBalance(
    uiState: AccountUiState.Success, showDialog: MutableState<Boolean>,
) {
    ListItem(
        modifier = Modifier
            .height(56.dp)
            .clickable { showDialog.value = true },
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
        content = { ContentTextListItem(uiState.account.name) },
        trail = {
            ContentTextListItem(uiState.account.balance)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "arrow",
                tint = colorScheme.onSurfaceVariant
            )
        },
        containerColor = colorScheme.secondaryContainer
    )
}

@Composable
fun AccountNameDialog(
    initialText: String = "",
    onCancel: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.name_account),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.enter_the_name)) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),

                    )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                    ) {
                        Text(stringResource(R.string.cancellation))
                    }

                    Button(
                        onClick = { onSave(text) },
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
private fun Diagram() {
    Icon(
        imageVector = Icons.Filled.Diagram,
        contentDescription = "Diagram",
        modifier = Modifier.fillMaxWidth(),
        tint = Color.Unspecified
    )
}


@Composable
private fun UpdateName(
    updateNameShowDialog: MutableState<Boolean>,
    updateName: (String) -> Unit
) {
    if (updateNameShowDialog.value) {
        AccountNameDialog(
            onCancel = { updateNameShowDialog.value = false },
            onSave = { accountName ->
                updateNameShowDialog.value = false
                updateName(accountName)
            }
        )
    }
}
