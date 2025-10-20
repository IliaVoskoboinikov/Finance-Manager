package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.screens

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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiCategory
import soft.divan.financemanager.feature.transaction.transaction_impl.R
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.mockCategories
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.mockTransactionUiStateSuccess
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel.TransactionViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
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
            onNavigateBack = { },
            onSave = { },
            onAmountChange = { },
            onCommentChange = { },
            onDateChange = { },
            onTimeChange = { },
            onCategoryChange = {}
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

@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    transactionId: Int? = null,
    isIncome: Boolean? = null,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        viewModel.load(transactionId, isIncome)
    }

    TransactionContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = { navController.popBackStack() },
        onSave = viewModel::createTransaction,
        onAmountChange = viewModel::updateAmount,
        onCommentChange = viewModel::updateComment,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onCategoryChange = viewModel::updateCategory,

        )
}

@Composable
fun TransactionContent(
    modifier: Modifier = Modifier,
    uiState: TransactionUiState,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onCategoryChange: (UiCategory) -> Unit,
) {

    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.my_expenses,
                    navigationIcon = Icons.Filled.Cross,
                    navigationIconClick = onNavigateBack,
                    actionIcon = Icons.Filled.ArrowConfirm,
                    actionIconClick = onSave
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is TransactionUiState.Loading -> LoadingProgressBar()
                is TransactionUiState.Error -> ErrorContent(onRetry = onSave)
                is TransactionUiState.Success -> TransactionForm(
                    uiState = uiState,
                    onAmountChange = onAmountChange,
                    onCommentChange = onCommentChange,
                    onDateChange = onDateChange,
                    onTimeChange = onTimeChange,
                    onCategoryChange = onCategoryChange,
                )
            }
        }
    }
}

@Composable
fun TransactionForm(
    uiState: TransactionUiState.Success,
    onAmountChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onCategoryChange: (UiCategory) -> Unit
) {
    val isShowDatePicker = remember { mutableStateOf(false) }
    val isShowTimePicker = remember { mutableStateOf(false) }
    val isShowCategorySheet = remember { mutableStateOf(false) }

    DataPicker(isShowDatePicker = isShowDatePicker, onDateChange = onDateChange)
    TimePicker(isShowTimePicker = isShowTimePicker, onTimeChange = onTimeChange)


    if (isShowCategorySheet.value) {
        CategoryBottomSheet(
            categories = uiState.categories,
            onCategorySelected = {
                onCategoryChange(it)
            },
            onDismissRequest = { isShowCategorySheet.value = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Account(uiState)
        FMDriver()
        Category(uiState = uiState, onClick = { isShowCategorySheet.value = true })
        FMDriver()
        Amount(uiState, onAmountChange)
        FMDriver()
        Data(isShowStartDatePicker = isShowDatePicker, uiState = uiState)
        FMDriver()
        Time(uiState, isShowTimePicker = isShowTimePicker)
        FMDriver()
        CommentInputField(
            value = uiState.transaction.comment,
            onValueChange = {
                onCommentChange(it)
            },
        )
    }
}

@Composable
private fun DataPicker(
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
private fun TimePicker(
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
private fun Account(uiState: TransactionUiState.Success) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
        content = { ContentTextListItem(stringResource(R.string.account)) },
        trail = {

            ContentTextListItem(uiState.accountName)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "arrow",
                tint = colorScheme.onSurfaceVariant

            )
        },
    )
}

@Composable
private fun Category(
    uiState: TransactionUiState.Success,
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
                ContentTextListItem(uiState.transaction.category.name)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Arrow,
                    contentDescription = "arrow",
                    tint = colorScheme.onSurfaceVariant
                )
            }
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    categories: List<UiCategory>,
    onCategorySelected: (UiCategory) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colorScheme.surfaceContainerHigh,
    ) {
        CategorySheetContent(categories, onCategorySelected, onDismissRequest)
    }
}

@Composable
private fun CategorySheetContent(
    categories: List<UiCategory>,
    onCategorySelected: (UiCategory) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(soft.divan.financemanager.feature.transaction.transaction_impl.R.string.select_category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        LazyColumn {
            items(categories) { category ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCategorySelected(category)
                            onDismissRequest()
                        }, content = {
                        Text(
                            text = category.name,
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
    uiState: TransactionUiState.Success,
    updateAmount: (String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
        content = { ContentTextListItem(stringResource(R.string.sum)) },
        trail = {

            BasicTextField(
                value = uiState.transaction.amount.toString(),
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        updateAmount(newValue)
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.End
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.width(200.dp),
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
private fun Data(
    isShowStartDatePicker: MutableState<Boolean>,
    uiState: TransactionUiState.Success,
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { isShowStartDatePicker.value = true },
        content = { ContentTextListItem(stringResource(R.string.data)) },
        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(uiState.transaction.transactionDate.toLocalDate())) },
    )
}

@Composable
private fun Time(uiState: TransactionUiState.Success, isShowTimePicker: MutableState<Boolean>) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { isShowTimePicker.value = true },
        content = { ContentTextListItem(stringResource(R.string.time)) },
        trail = { ContentTextListItem(DateHelper.formatTimeForDisplay(uiState.transaction.transactionDate)) },
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = colorScheme.outline
                                ),
                                maxLines = maxLines
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        FMDriver()
    }
}


