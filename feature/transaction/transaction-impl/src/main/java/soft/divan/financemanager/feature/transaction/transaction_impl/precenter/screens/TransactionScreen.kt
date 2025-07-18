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
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel.TransactionViewModel
import soft.divan.financemanager.string.R
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
        TransactionScreen(navController = rememberNavController())
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
    val showStartDatePicker = remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {

        viewModel.load(transactionId, false)

    }
    when (uiState) {
        is TransactionUiState.Error -> {
            ErrorContent({})
        }

        TransactionUiState.Loading -> {
            LoadingProgressBar()
        }

        is TransactionUiState.Success -> {
            val state = (uiState as TransactionUiState.Success)

            Box(modifier = modifier.fillMaxSize()) {

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
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        topBar = TopBarModel(
                            title = R.string.my_expenses,
                            navigationIcon = Icons.Filled.Cross,
                            navigationIconClick = { navController.popBackStack() },
                            actionIcon = Icons.Filled.ArrowConfirm,
                            actionIconClick = {
                                viewModel.createTransaction()
                            })
                    )

                    ListItem(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        content = { ContentTextListItem(stringResource(R.string.account)) },
                        trail = {

                            ContentTextListItem(state.accountName)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Filled.Arrow,
                                contentDescription = "arrow",
                                tint = colorScheme.onSurfaceVariant

                            )
                        },
                    )
                    FMDriver()

                    ListItem(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        content = { ContentTextListItem(stringResource(R.string.category)) },
                        trail = {
                            ContentTextListItem(state.transaction.category.name)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Filled.Arrow,
                                contentDescription = "arrow",
                                tint = colorScheme.onSurfaceVariant

                            )
                        },
                    )
                    FMDriver()

                    ListItem(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        content = { ContentTextListItem(stringResource(R.string.sum)) },
                        trail = {

                            BasicTextField(
                                value = state.transaction.amount.toString(),
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        viewModel.updateAmount(newValue)
                                    }
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
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
                    FMDriver()

                    ListItem(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth()
                            .clickable { showStartDatePicker.value = true },
                        content = { ContentTextListItem(stringResource(R.string.data)) },
                        trail = { ContentTextListItem(state.transaction.transactionDate.toString()) },
                    )
                    FMDriver()

                    ListItem(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        content = { ContentTextListItem(stringResource(R.string.time)) },
                        trail = { ContentTextListItem("state.sumTransaction") },
                    )
                    FMDriver()

                    CommentInputField(
                        value = state.transaction.comment,
                        onValueChange = {
                            viewModel.updateComment(it)
                        },
                        modifier = modifier
                    )
                }
            }
        }
    }
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

        FMDriver()
    }
}

