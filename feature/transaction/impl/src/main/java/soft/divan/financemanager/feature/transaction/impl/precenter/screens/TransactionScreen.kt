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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.transaction.impl.R
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
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


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TransactionScreenPreview() {
    FinanceManagerTheme {
        TransactionContent(
            uiState = mockTransactionUiStateSuccess,
            isIncome = false,
            onNavigateBack = { },
            onSave = { },
            onAmountChange = { },
            onCommentChange = { },
            onDateChange = { },
            onTimeChange = { },
            onCategoryChange = {},
            onAccountChange = {},
            onDelete = {},
            snackbarHostState = remember { SnackbarHostState() }
        )

    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CategoryPreview() {
    FinanceManagerTheme {
        CategorySheetContent(mockCategories, {}, {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountPreview() {
    FinanceManagerTheme {
        AccountSheetContent(mockAccounts, {}, {})
    }
}

@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    isIncome: Boolean,
    viewModel: TransactionViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TransactionEvent.TransactionDeleted -> onNavigateBack()

                is TransactionEvent.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.messageRes),
                        withDismissAction = true
                    )

                is TransactionEvent.TransactionSaved -> onNavigateBack()
            }
        }
    }

    TransactionContent(
        modifier = modifier,
        uiState = uiState,
        isIncome = isIncome,
        onNavigateBack = onNavigateBack,
        onSave = viewModel::save,
        onAmountChange = viewModel::onAmountInputChanged,
        onCommentChange = viewModel::updateComment,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onCategoryChange = viewModel::updateCategory,
        onAccountChange = viewModel::updateAccount,
        onDelete = { viewModel.delete() },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun TransactionContent(
    modifier: Modifier = Modifier,
    isIncome: Boolean,
    uiState: TransactionUiState,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onCategoryChange: (CategoryUi) -> Unit,
    onAccountChange: (AccountUi) -> Unit,
    onDelete: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    Scaffold(
        topBar = { TopBarTransaction(isIncome, onNavigateBack, onSave) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is TransactionUiState.Loading -> LoadingProgressBar()
                is TransactionUiState.Error -> ErrorContent(onClick = onSave)
                is TransactionUiState.Success -> TransactionForm(
                    modifier = modifier,
                    uiState = uiState,
                    onAmountChange = onAmountChange,
                    onCommentChange = onCommentChange,
                    onDateChange = onDateChange,
                    onTimeChange = onTimeChange,
                    onCategoryChange = onCategoryChange,
                    onAccountChange = onAccountChange,
                    onDelete = onDelete
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
    modifier: Modifier = Modifier,
    uiState: TransactionUiState.Success,
    onAmountChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onCategoryChange: (CategoryUi) -> Unit,
    onAccountChange: (AccountUi) -> Unit,
    onDelete: () -> Unit
) {
    val isShowAccountsSheet = remember { mutableStateOf(false) }
    val isShowDatePicker = remember { mutableStateOf(false) }
    val isShowTimePicker = remember { mutableStateOf(false) }
    val isShowCategorySheet = remember { mutableStateOf(false) }
    val isShowDeleteDialog = remember { mutableStateOf(false) }

    ShowDataPickerDialog(isShowDatePicker = isShowDatePicker, onDateChange = onDateChange)
    ShowTimePickerDialog(isShowTimePicker = isShowTimePicker, onTimeChange = onTimeChange)
    ShowCategoryBottomSheet(
        isShowCategorySheet = isShowCategorySheet,
        categories = uiState.categories,
        onCategoryChange = onCategoryChange
    )
    ShowAccountsBottomSheet(
        isShowAccountsSheet = isShowAccountsSheet,
        accounts = uiState.accounts,
        onAccountChange = onAccountChange
    )

    ShowDeleteDialog(isShowDeleteDialog, onDelete)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Account(uiState = uiState, onClick = { isShowAccountsSheet.value = true })
        FMDriver()
        Category(
            category = uiState.transaction.category.emoji + " " + uiState.transaction.category.name,
            onClick = { isShowCategorySheet.value = true })
        FMDriver()
        Amount(amount = uiState.transaction.amount, onAmountChange = onAmountChange)
        FMDriver()
        Data(
            transactionDate = uiState.transaction.date,
            isShowStartDatePicker = isShowDatePicker
        )
        FMDriver()
        Time(
            transactionDate = uiState.transaction.time,
            isShowTimePicker = isShowTimePicker
        )
        FMDriver()
        CommentInputField(
            value = uiState.transaction.comment,
            onValueChange = {
                onCommentChange(it)
            },
        )
        FMDriver()
        Spacer(modifier = Modifier.height(24.dp))
        if (uiState.transaction.mode is TransactionMode.Edit)
            DeleteButton({ isShowDeleteDialog.value = true })
    }
}

@Composable
private fun ShowDataPickerDialog(
    isShowDatePicker: MutableState<Boolean>,
    onDateChange: (LocalDate) -> Unit
) {
    if (isShowDatePicker.value) {
        FMDatePickerDialog(
            initialDate = LocalDate.now(),
            onDateSelected = { date ->
                onDateChange(date)
            },
            onDismissRequest = { isShowDatePicker.value = false }
        )
    }
}

@Composable
private fun ShowTimePickerDialog(
    isShowTimePicker: MutableState<Boolean>,
    onTimeChange: (LocalTime) -> Unit
) {
    if (isShowTimePicker.value) {
        FMTimePickerDialog(
            initialTime = LocalTime.now(),
            onTimeSelected = { time ->
                onTimeChange(time)
            },
            onDismissRequest = {
                isShowTimePicker.value = false
            }
        )
    }
}

@Composable
private fun ShowCategoryBottomSheet(
    isShowCategorySheet: MutableState<Boolean>,
    categories: List<CategoryUi>,
    onCategoryChange: (CategoryUi) -> Unit
) {
    if (isShowCategorySheet.value) {
        CategoryBottomSheet(
            categories = categories,
            onCategorySelected = {
                onCategoryChange(it)
            },
            onDismissRequest = { isShowCategorySheet.value = false }
        )
    }
}

@Composable
private fun ShowAccountsBottomSheet(
    isShowAccountsSheet: MutableState<Boolean>,
    accounts: List<AccountUi>,
    onAccountChange: (AccountUi) -> Unit
) {
    if (isShowAccountsSheet.value) {
        AccountsBottomSheet(
            accounts = accounts,
            onAccountsSelected = {
                onAccountChange(it)
            },
            onDismissRequest = { isShowAccountsSheet.value = false }
        )
    }
}


@Composable
private fun ShowDeleteDialog(isShowDeleteDialog: MutableState<Boolean>, onDelete: () -> Unit) {
    if (isShowDeleteDialog.value) {
        DeleteDialog(isShowDeleteDialog, onDelete)
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
            ContentTextListItem(
                uiState.accounts.find { it.id == uiState.transaction.accountId }?.name ?: ""
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "arrow",
                tint = MaterialTheme.colorScheme.onSurfaceVariant

            )
        },
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
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    categories: List<CategoryUi>,
    onCategorySelected: (CategoryUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        CategorySheetContent(categories, onCategorySelected, onDismissRequest)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsBottomSheet(
    accounts: List<AccountUi>,
    onAccountsSelected: (AccountUi) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        AccountSheetContent(accounts, onAccountsSelected, onDismissRequest)
    }
}


@Composable
private fun AccountSheetContent(
    accounts: List<AccountUi>,
    onAccountSelected: (AccountUi) -> Unit,
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
                            onAccountSelected(account)
                            onDismissRequest()
                        }, content = {
                        Text(
                            text = account.name + " " + account.balance + " " + account.currency,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    })
                FMDriver()

            }
        }
    }
}

@Composable
private fun CategorySheetContent(
    categories: List<CategoryUi>,
    onCategorySelected: (CategoryUi) -> Unit,
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
                            onCategorySelected(category)
                            onDismissRequest()
                        }, content = {
                        Text(
                            text = category.emoji + " " + category.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    })
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
    isShowStartDatePicker: MutableState<Boolean>,
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { isShowStartDatePicker.value = true },
        content = { ContentTextListItem(stringResource(R.string.data)) },
        trail = { ContentTextListItem(transactionDate) },
    )
}

@Composable
private fun Time(transactionDate: String, isShowTimePicker: MutableState<Boolean>) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { isShowTimePicker.value = true },
        content = { ContentTextListItem(stringResource(R.string.time)) },
        trail = { ContentTextListItem(transactionDate) },
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


