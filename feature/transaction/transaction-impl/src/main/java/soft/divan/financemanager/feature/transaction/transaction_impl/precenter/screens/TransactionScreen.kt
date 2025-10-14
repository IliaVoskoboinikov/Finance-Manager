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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
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
import soft.divan.financemanager.core.string.R
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.mockTransactionUiStateSuccess
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel.TransactionViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDatePickerDialog
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.ArrowConfirm
import soft.divan.financemanager.uikit.icons.Cross
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import java.time.LocalDate


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TransactionScreenPreview() {
    FinanceManagerTheme {
        TransactionContent(
            uiState = mockTransactionUiStateSuccess,
            popBackStack = {},
            createTransaction = { },
            updateAmount = { },
            updateComment = {}
        )
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
        viewModel.load(transactionId, false)

    }

    TransactionContent(
        modifier = modifier,
        uiState = uiState,
        transactionId = transactionId,
        isIncome = isIncome,
        popBackStack = { navController.popBackStack() },
        createTransaction = { viewModel.createTransaction() },
        updateAmount = { viewModel.updateAmount(it) },
        updateComment = { viewModel.updateComment(it) }
    )
}

@Composable
fun TransactionContent(
    modifier: Modifier = Modifier,
    uiState: TransactionUiState,
    transactionId: Int? = null,
    isIncome: Boolean? = null,
    popBackStack: () -> Unit,
    createTransaction: () -> Unit,
    updateAmount: (String) -> Unit,
    updateComment: (String) -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.my_expenses,
                    navigationIcon = Icons.Filled.Cross,
                    navigationIconClick = { popBackStack() },
                    actionIcon = Icons.Filled.ArrowConfirm,
                    actionIconClick = { createTransaction() })
            )

            when (uiState) {
                is TransactionUiState.Error -> {
                    ErrorContent({})
                }

                is TransactionUiState.Loading -> {
                    LoadingProgressBar()
                }

                is TransactionUiState.Success -> {
                    UiStateSuccessContent(
                        uiState = uiState,
                        updateAmount = updateAmount,
                        updateComment = updateComment
                    )
                }
            }
        }
    }
}

@Composable
fun UiStateSuccessContent(
    uiState: TransactionUiState.Success,
    updateAmount: (String) -> Unit,
    updateComment: (String) -> Unit,
) {
    val showStartDatePicker = remember { mutableStateOf(false) }
    if (showStartDatePicker.value) {
        FMDatePickerDialog(
            initialDate = LocalDate.now(),
            onDateSelected = {
                /* startDate = it
                 viewModel.updateStartDate(startDate)
                 viewModel.loadHistory(startDate, endDate)*/
                showStartDatePicker.value = false
            },
            onDismissRequest = { showStartDatePicker.value = false }
        )
    }

    Account(uiState)
    FMDriver()

    Category(uiState)

    FMDriver()

    Amount(uiState, updateAmount)
    FMDriver()

    Data(showStartDatePicker, uiState)
    FMDriver()

    Time(uiState)
    FMDriver()

    CommentInputField(
        value = uiState.transaction.comment,
        onValueChange = {
            updateComment(it)
        },
    )
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
private fun Category(uiState: TransactionUiState.Success) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
        content = { ContentTextListItem(stringResource(R.string.category)) },
        trail = {
            ContentTextListItem(uiState.transaction.category.name)
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
                modifier = Modifier.width(70.dp),
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
    showStartDatePicker: MutableState<Boolean>,
    uiState: TransactionUiState.Success
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable { showStartDatePicker.value = true },
        content = { ContentTextListItem(stringResource(R.string.data)) },
        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(uiState.transaction.transactionDate.toLocalDate())) },
    )
}

@Composable
private fun Time(uiState: TransactionUiState.Success) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
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

