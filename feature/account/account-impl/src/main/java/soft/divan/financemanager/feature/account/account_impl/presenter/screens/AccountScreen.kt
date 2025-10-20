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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.core.domain.model.CurrencySymbol
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
import soft.divan.financemanager.uikit.icons.MdiDollar
import soft.divan.financemanager.uikit.icons.MdiEuro
import soft.divan.financemanager.uikit.icons.MdiRuble
import soft.divan.financemanager.uikit.icons.Pencil
import soft.divan.financemanager.uikit.icons.RoundCross
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
            ), updateCurrency = {}, updateName = {}, navController = rememberNavController()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCurrencySheet() {
    FinanceManagerTheme {
        CurrencySheetContent({}, {})
    }
}


@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountContent(
        modifier = modifier,
        uiState = uiState,
        updateCurrency = viewModel::updateCurrency,
        updateName = viewModel::updateName,
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    updateCurrency: (String) -> Unit,
    updateName: (String) -> Unit,
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                topBar = TopBarModel(
                    title = R.string.my_account,
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
                    UiStateSuccessContent(uiState, updateCurrency, updateName)
                }
            }
        }
        FloatingButton(
            onClick = {},
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

    updateCurrency: (String) -> Unit,
    updateName: (String) -> Unit
) {
    val currencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isCurrencySheetOpen = remember { mutableStateOf(false) }
    val updateNameShowDialog = remember { mutableStateOf(false) }

    Column() {
        AccountBalance(uiState, updateNameShowDialog)
        FMDriver()
        AccountCurrency(isCurrencySheetOpen, uiState)
        Spacer(modifier = Modifier.height(16.dp))
        Diagram()
        ChoiceCurrency(isCurrencySheetOpen, currencySheetState, updateCurrency)
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
private fun AccountCurrency(
    isSheetOpen: MutableState<Boolean>,
    uiState: AccountUiState.Success
) {
    ListItem(
        modifier = Modifier
            .height(56.dp)
            .clickable { isSheetOpen.value = true },
        content = { ContentTextListItem(stringResource(R.string.currency)) },
        trail = {
            ContentTextListItem(uiState.account.currency)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "AccountCurrency",
                tint = colorScheme.onSurfaceVariant
            )
        },
        containerColor = colorScheme.secondaryContainer
    )
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
@OptIn(ExperimentalMaterial3Api::class)
private fun ChoiceCurrency(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    updateCurrency: (String) -> Unit
) {
    if (isSheetOpen.value) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen.value = false },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            CurrencySheetContent(
                onCancel = { isSheetOpen.value = false },
                updateCurrency = updateCurrency
            )
        }
    }

}

@Composable
fun CurrencySheetContent(onCancel: () -> Unit, updateCurrency: (String) -> Unit) {
    val currencies = listOf(
        Triple(Icons.Filled.MdiRuble, R.string.rub, CurrencySymbol.RUB.symbol),
        Triple(Icons.Filled.MdiDollar, R.string.usd, CurrencySymbol.USD.symbol),
        Triple(Icons.Filled.MdiEuro, R.string.eur, CurrencySymbol.EUR.symbol)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        currencies.forEach { (icon, name, symbol) ->
            CurrencyItem(icon = icon, title = name, symbol, onClick = {
                updateCurrency(symbol)
                onCancel()
            })
            FMDriver()
        }

        ListItem(
            modifier = Modifier
                .background(color = colorScheme.error)
                .clickable { onCancel() },
            lead = {
                Icon(
                    imageVector = Icons.Filled.RoundCross,
                    contentDescription = stringResource(R.string.cancellation),
                    tint = Color.White
                )
            },
            content = {
                ContentTextListItem(
                    text = stringResource(R.string.cancellation),
                    color = Color.White
                )
            },
        )

    }
}

@Composable
fun CurrencyItem(icon: ImageVector, title: Int, symbol: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        lead = {
            Icon(
                imageVector = icon,
                contentDescription = "Currency",
                tint = Color.Unspecified
            )
        },
        content = { ContentTextListItem(text = stringResource(title) + " $symbol") },
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
