package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.mapper.CurrencySymbol
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.icons.Diagram
import soft.divan.financemanager.presenter.ui.icons.MdiDollar
import soft.divan.financemanager.presenter.ui.icons.MdiEuro
import soft.divan.financemanager.presenter.ui.icons.MdiRuble
import soft.divan.financemanager.presenter.ui.icons.RoundCross
import soft.divan.financemanager.presenter.ui.model.AccountUiModel
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.AccountViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar


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
            ), updateCurrency = {}, navController = rememberNavController()
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
        updateCurrency = viewModel::updateCensure,
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    updateCurrency: (String) -> Unit,
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { FloatingButton(onClick = { navController.navigate(AddAccountScreen.route) }) }
    ) { innerPadding ->
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
                UiStateSuccessContent(innerPadding, uiState, updateCurrency)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UiStateSuccessContent(
    innerPadding: PaddingValues,
    uiState: AccountUiState.Success,

    updateCurrency: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSheetOpen = remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(innerPadding)) {
        AccountBalance(uiState)
        FMDriver()
        AccountCurrency(isSheetOpen, uiState)
        Spacer(modifier = Modifier.height(16.dp))
        Diagram()
        ChoiceCurrency(isSheetOpen, sheetState, updateCurrency)
    }
}

@Composable
private fun AccountBalance(uiState: AccountUiState.Success) {
    ListItem(
        modifier = Modifier.height(56.dp),
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
