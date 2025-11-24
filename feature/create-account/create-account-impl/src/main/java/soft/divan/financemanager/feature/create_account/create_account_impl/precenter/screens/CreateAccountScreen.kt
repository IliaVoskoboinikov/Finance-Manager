package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.create_account.create_account_impl.R
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountEvent
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountUiState
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.mockCreateAccountUiStateSuccess
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.viewModel.CreateAccountViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.SaveButton
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.ArrowConfirm
import soft.divan.financemanager.uikit.icons.Cross
import soft.divan.financemanager.uikit.icons.MdiDollar
import soft.divan.financemanager.uikit.icons.MdiEuro
import soft.divan.financemanager.uikit.icons.MdiRuble
import soft.divan.financemanager.uikit.icons.RoundCross
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CreateAccountScreenPreview() {
    FinanceManagerTheme {
        CreateAccountContent(
            uiState = mockCreateAccountUiStateSuccess,
            onNavigateBack = { },
            onUpdateName = {},
            onUpdateBalance = {},
            onUpdateCurrency = {},
            onSave = { },
            snackbarHostState = remember { SnackbarHostState() }
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
        onUpdateName = viewModel::updateName,
        onUpdateBalance = viewModel::updateBalance,
        onUpdateCurrency = viewModel::updateCurrency,
        onSave = viewModel::createAccount,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun CreateAccountContent(
    modifier: Modifier = Modifier,
    uiState: CreateAccountUiState,
    onNavigateBack: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateBalance: (String) -> Unit,
    onUpdateCurrency: (String) -> Unit,
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
                    onUpdateName = onUpdateName,
                    onUpdateBalance = onUpdateBalance,
                    onUpdateCurrency = onUpdateCurrency,
                    onSave = onSave
                )
            }
        }
    }
}

//todo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountForm(
    uiState: CreateAccountUiState.Success,
    onUpdateName: (String) -> Unit,
    onUpdateBalance: (String) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onSave: () -> Unit
) {
    val currencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isCurrencySheetOpen = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Name(uiState.account.name, onUpdateName)
        FMDriver()
        Balance(uiState, onUpdateBalance)
        FMDriver()
        Currency(
            isSheetOpen = isCurrencySheetOpen,
            currency = uiState.account.currency,
            sheetState = currencySheetState,
            updateCurrency = onUpdateCurrency
        )
        FMDriver()
        Spacer(modifier = Modifier.height(24.dp))
        SaveButton(onSave)
    }
}

@Composable
private fun Balance(
    uiState: CreateAccountUiState.Success,
    onUpdateBalance: (String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { },
        lead = { ContentTextListItem("Баланс") },
        content = {
            BasicTextField(
                value = uiState.account.balance,
                onValueChange = { newValue ->
                    val moneyRegex = Regex("^(0|[1-9]\\d*)(\\.\\d{0,2})?$")

                    when {
                        newValue.isEmpty() -> {
                            onUpdateBalance("0")
                        }

                        uiState.account.balance.toString() == "0" &&
                                newValue.length == 2 &&
                                newValue.startsWith("0") &&
                                newValue[1].isDigit() -> {
                            onUpdateBalance(newValue.last().toString())
                        }

                        newValue.matches(moneyRegex) -> {

                            onUpdateBalance(newValue)
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = false,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        innerTextField()
                    }

                }
            )
        },
    )
}

@Composable
private fun Name(
    balance: String,
    onUpdateName: (String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { },
        lead = { ContentTextListItem("Название") },
        content = {

            BasicTextField(
                value = balance,
                onValueChange = { onUpdateName(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = false,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        innerTextField()
                    }

                }
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Currency(
    isSheetOpen: MutableState<Boolean>,
    currency: String,
    sheetState: SheetState,
    updateCurrency: (String) -> Unit
) {
    ChoiceCurrency(isSheetOpen, sheetState, updateCurrency)

    ListItem(
        modifier = Modifier
            .height(56.dp)
            .clickable { isSheetOpen.value = true },
        content = { ContentTextListItem(stringResource(R.string.currency)) },
        trail = {
            ContentTextListItem(currency)
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

            modifier = Modifier.clickable { onCancel() },
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
            containerColor = colorScheme.error
        )

    }
}
