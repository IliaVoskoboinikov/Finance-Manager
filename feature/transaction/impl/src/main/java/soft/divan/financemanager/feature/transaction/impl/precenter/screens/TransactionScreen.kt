package soft.divan.financemanager.feature.transaction.impl.precenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.transaction.api.TransactionKey
import soft.divan.financemanager.feature.transaction.impl.R
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionActions
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.impl.precenter.model.mockAccounts
import soft.divan.financemanager.feature.transaction.impl.precenter.model.mockCategories
import soft.divan.financemanager.feature.transaction.impl.precenter.model.mockTransactionUiStateSuccess
import soft.divan.financemanager.feature.transaction.impl.precenter.viewModel.TransactionViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.DeleteButton
import soft.divan.financemanager.uikit.components.DeleteDialog
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDatePickerDialog
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.FMTimePickerDialog
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.ArrowConfirm
import soft.divan.financemanager.uikit.icons.Cross
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import java.time.LocalDate
import java.time.LocalTime

@NavPreview(route = TransactionKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TransactionScreenPreview() {
    FinanceManagerTheme {
        TransactionContent(
            uiState = mockTransactionUiStateSuccess,
            isIncome = false,
            actions = TransactionActions(
                onNavigateBack = { },
                onSave = { },
                onAmountChange = { },
                onCommentChange = { },
                onDateChange = { },
                onTimeChange = { },
                onCategoryChange = { },
                onAccountChange = { },
                onDelete = { }
            ),
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CategoryPreview() {
    FinanceManagerTheme {
        CategorySheetContent(mockCategories, {}, {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccountPreview() {
    FinanceManagerTheme {
        AccountSheetContent(mockAccounts, {}, {})
    }
}

@NavDestination(route = TransactionKey::class)
@Composable
fun TransactionScreen(
    isIncome: Boolean,
    transactionId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel =
        hiltViewModel<TransactionViewModel, TransactionViewModel.Factory> { factory ->
            factory.create(isIncome = isIncome, transactionId = transactionId)
        },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TransactionEvent.TransactionDeleted -> currentOnNavigateBack()

                is TransactionEvent.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = resources.getString(event.messageRes),
                        withDismissAction = true
                    )

                is TransactionEvent.TransactionSaved -> currentOnNavigateBack()
            }
        }
    }

    TransactionContent(
        modifier = modifier,
        uiState = uiState,
        isIncome = isIncome,
        actions = TransactionActions(
            onNavigateBack = onNavigateBack,
            onSave = viewModel::save,
            onAmountChange = viewModel::onAmountInputChanged,
            onCommentChange = viewModel::updateComment,
            onDateChange = viewModel::updateDate,
            onTimeChange = viewModel::updateTime,
            onCategoryChange = viewModel::updateCategory,
            onAccountChange = viewModel::updateAccount,
            onDelete = viewModel::delete
        ),
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun TransactionContent(
    isIncome: Boolean,
    uiState: TransactionUiState,
    actions: TransactionActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBarTransaction(isIncome, actions.onNavigateBack, actions.onSave) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is TransactionUiState.Loading -> LoadingProgressBar()

                is TransactionUiState.Error -> ErrorContent(onClick = actions.onSave)

                is TransactionUiState.Success -> TransactionForm(
                    uiState = uiState,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun TopBarTransaction(
    isIncome: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    TopBar(
        topBar = TopBarModel(
            title = if (isIncome) R.string.my_income else R.string.my_expenses,
            navigationIcon = Icons.Filled.Cross,
            navigationIconClick = onNavigateBack,
            actionIcon = Icons.Filled.ArrowConfirm,
            actionIconClick = onSave
        )
    )
}

@Composable
fun TransactionForm(
    uiState: TransactionUiState.Success,
    actions: TransactionActions,
    modifier: Modifier = Modifier
) {
    var isShowAccountsSheet by remember { mutableStateOf(false) }
    var isShowDatePicker by remember { mutableStateOf(false) }
    var isShowTimePicker by remember { mutableStateOf(false) }
    var isShowCategorySheet by remember { mutableStateOf(false) }
    var isShowDeleteDialog by remember { mutableStateOf(false) }

    ShowDataPickerDialog(isShowDatePicker, { isShowDatePicker = false }, actions.onDateChange)
    ShowTimePickerDialog(isShowTimePicker, { isShowTimePicker = false }, actions.onTimeChange)
    ShowCategoryBottomSheet(
        isVisible = isShowCategorySheet,
        onDismiss = { isShowCategorySheet = false },
        categories = uiState.categories,
        onCategoryChange = actions.onCategoryChange
    )
    ShowAccountsBottomSheet(
        isVisible = isShowAccountsSheet,
        onDismiss = { isShowAccountsSheet = false },
        accounts = uiState.accounts.filterNot { it.archived },
        onAccountChange = actions.onAccountChange
    )

    ShowDeleteDialog(isShowDeleteDialog, { isShowDeleteDialog = false }, actions.onDelete)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Account(uiState = uiState, onClick = { isShowAccountsSheet = true })
        FMDriver()
        Category(
            category = uiState.transaction.category.emoji + " " + uiState.transaction.category.name,
            onClick = { isShowCategorySheet = true }
        )
        FMDriver()
        Amount(amount = uiState.transaction.amount, onAmountChange = actions.onAmountChange)
        FMDriver()
        Data(
            transactionDate = uiState.transaction.date,
            onClick = { isShowDatePicker = true }
        )
        FMDriver()
        Time(
            transactionDate = uiState.transaction.time,
            onClick = { isShowTimePicker = true }
        )
        FMDriver()
        CommentInputField(
            value = uiState.transaction.comment,
            onValueChange = {
                actions.onCommentChange(it)
            }
        )
        FMDriver()
        Spacer(modifier = Modifier.height(24.dp))
        if (uiState.transaction.mode is TransactionMode.Edit) {
            DeleteButton(onClick = { isShowDeleteDialog = true })
        }
    }
}

@Composable
private fun ShowDataPickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    if (isVisible) {
        FMDatePickerDialog(
            initialDate = LocalDate.now(),
            onDateSelect = onDateChange,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
private fun ShowTimePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onTimeChange: (LocalTime) -> Unit
) {
    if (isVisible) {
        FMTimePickerDialog(
            initialTime = LocalTime.now(),
            onTimeSelect = onTimeChange,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
private fun ShowCategoryBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    categories: List<CategoryUi>,
    onCategoryChange: (CategoryUi) -> Unit
) {
    if (isVisible) {
        CategoryBottomSheet(
            categories = categories,
            onCategorySelect = onCategoryChange,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
private fun ShowAccountsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    accounts: List<AccountUi>,
    onAccountChange: (AccountUi) -> Unit
) {
    if (isVisible) {
        AccountsBottomSheet(
            accounts = accounts,
            onAccountSelect = onAccountChange,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
private fun ShowDeleteDialog(isVisible: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit) {
    if (isVisible) {
        DeleteDialog(onDismissRequest = onDismiss, onDelete = onDelete)
    }
}

@Composable
private fun Account(
    uiState: TransactionUiState.Success,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        content = { ContentTextListItem(stringResource(R.string.account)) },
        trail = {
            val selectedAccount =
                uiState.accounts.find { it.id == uiState.transaction.accountId }
            val accountName = when {
                selectedAccount == null -> ""

                selectedAccount.archived ->
                    selectedAccount.name + stringResource(R.string.archived_account_suffix)

                else -> selectedAccount.name
            }
            ContentTextListItem(accountName)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "arrow",
                tint = MaterialTheme.colorScheme.onSurfaceVariant

            )
        }
    )
}

@Composable
private fun Category(
    category: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        content = { ContentTextListItem(stringResource(R.string.category)) },
        trail = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ContentTextListItem(category)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Arrow,
                    contentDescription = "arrow",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    categories: List<CategoryUi>,
    onCategorySelect: (CategoryUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        CategorySheetContent(categories, onCategorySelect, onDismissRequest)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsBottomSheet(
    accounts: List<AccountUi>,
    onAccountSelect: (AccountUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        AccountSheetContent(accounts, onAccountSelect, onDismissRequest)
    }
}

@Composable
private fun AccountSheetContent(
    accounts: List<AccountUi>,
    onAccountSelect: (AccountUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.select_account),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        LazyColumn {
            items(
                items = accounts,
                key = { it.id }
            ) { account ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAccountSelect(account)
                            onDismissRequest()
                        },
                    content = {
                        Text(
                            text = "${account.name} ${account.balance} " +
                                CurrencySymbol.fromId(account.currencyId),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
                FMDriver()
            }
        }
    }
}

@Composable
private fun CategorySheetContent(
    categories: List<CategoryUi>,
    onCategorySelect: (CategoryUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.select_category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        LazyColumn {
            items(
                items = categories,
                key = { it.id }
            ) { category ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCategorySelect(category)
                            onDismissRequest()
                        },
                    content = {
                        Text(
                            text = category.emoji + " " + category.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
                FMDriver()
            }
        }
    }
}

@Composable
private fun Amount(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
        content = { ContentTextListItem(stringResource(R.string.sum)) },
        trail = {
            AmountTextField(
                value = amount,
                onValueChange = onAmountChange
            )
        }
    )
}

@Composable
private fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true,
        modifier = Modifier
            .width(200.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxHeight()
            ) {
                innerTextField()
            }
        }
    )
}

@Composable
private fun Data(
    transactionDate: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        content = { ContentTextListItem(stringResource(R.string.data)) },
        trail = { ContentTextListItem(transactionDate) }
    )
}

@Composable
private fun Time(transactionDate: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        content = { ContentTextListItem(stringResource(R.string.time)) },
        trail = { ContentTextListItem(transactionDate) }
    )
}

@Composable
fun CommentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.comment),
    maxLines: Int = 2
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = false,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.outline
                                ),
                                maxLines = maxLines
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
