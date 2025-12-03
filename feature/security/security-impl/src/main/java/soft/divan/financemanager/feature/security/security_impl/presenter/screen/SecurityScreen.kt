package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.security_impl.R
import soft.divan.financemanager.feature.security.security_impl.presenter.model.SecurityUiState
import soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel.SecurityViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewSecurityScreen() {
    FinanceManagerTheme {
        SecurityContent(
            uiState = SecurityUiState.Success(pin = "2122", hasPin = true),
            deletePin = {},
            onNavigateBack = {},
            onNavigateToCreatePin = {}
        )
    }
}

@Composable
fun SecurityScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePin: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SecurityContent(
        modifier = modifier,
        uiState = uiState,
        deletePin = viewModel::deletePin,
        onNavigateBack = onNavigateBack,
        onNavigateToCreatePin = onNavigateToCreatePin
    )
}

@Composable
fun SecurityContent(
    modifier: Modifier = Modifier,
    uiState: SecurityUiState,
    deletePin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePin: () -> Unit
) {
    Scaffold(
        topBar = { TopBarSecurity(onNavigateBack) },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is SecurityUiState.Error -> ErrorContent(onRetry = {})
                is SecurityUiState.Loading -> LoadingProgressBar()
                is SecurityUiState.Success -> SecuritySucsessState(
                    uiState = uiState,
                    onNavigateToCreatePin = onNavigateToCreatePin,
                    deletePin = deletePin
                )
            }
        }
    }
}

@Composable
private fun SecuritySucsessState(
    uiState: SecurityUiState.Success,
    onNavigateToCreatePin: () -> Unit,
    deletePin: () -> Unit
) {

    Spacer(modifier = Modifier.size(50.dp))
    Btn(textRes = R.string.create_pin, onClick = onNavigateToCreatePin)
    Spacer(modifier = Modifier.size(50.dp))
    if (uiState.hasPin)
        Btn(textRes = R.string.delete_pin, onClick = deletePin)

}

@Composable
private fun TopBarSecurity(onNavigateBack: () -> Unit) {
    TopBar(
        topBar = TopBarModel(
            title = R.string.security,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationIconClick = { onNavigateBack() },
        )
    )
}

@Composable
private fun Btn(textRes: Int, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ) {
        Text(text = stringResource(textRes))
    }
}