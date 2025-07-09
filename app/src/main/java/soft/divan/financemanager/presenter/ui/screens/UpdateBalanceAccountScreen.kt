package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.presenter.ui.model.UpdateBalanceAccountUiState
import soft.divan.financemanager.presenter.ui.viewmodel.UpdateBalanceAccountViewModel
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun UpdateBalanceAccountScreenPreview() {
    FinanceManagerTheme {
        /* UpdateBalanceContent()*/
    }
}


object AddAccountScreen {
    const val route = "add_account"
}

//update balance account
@Composable
fun UpdateBalanceAccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: UpdateBalanceAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(null)
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    UpdateBalanceContent(
        modifier = modifier, balance = balance, onBalanceChanged = viewModel::onBalanceChanged
    )

    when (uiState) {
        is UpdateBalanceAccountUiState.Error -> {}
        is UpdateBalanceAccountUiState.Loading -> {}
        is UpdateBalanceAccountUiState.Success -> {
            navController.popBackStack()
        }

        null -> {}
    }
}

@Composable
fun UpdateBalanceContent(
    modifier: Modifier = Modifier,
    balance: String,
    onBalanceChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    Column() {
        BalanceInputListItem(
            value = balance,
            onValueChange = onBalanceChanged
        )
        FMDriver()
    }

}


@Composable
fun BalanceInputListItem(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ListItem(
        modifier = modifier.height(56.dp),
        lead = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        content = {
            Text(
                text = "Баланс",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        trail = {
            Box(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 150.dp)
                    .focusRequester(focusRequester)
            ) {

                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val formatted = newValue.replace(',', '.')
                        if (formatted.matches(Regex("-?[0-9]*\\.?[0-9]*"))) {
                            onValueChange(formatted)
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),

                    modifier = Modifier
                        .widthIn(min = 80.dp, max = 150.dp)
                        .padding(end = 4.dp)
                )
            }
        },
        containerColor = Color.Transparent
    )
}
