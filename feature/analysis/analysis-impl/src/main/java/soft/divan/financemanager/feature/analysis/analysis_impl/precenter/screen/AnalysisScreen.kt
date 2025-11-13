package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.analysis.analysis_impl.R
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.AnalysisUiState
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.mockTransactionUiStateLoading
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.viewModel.AnalysisViewModel
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AnalysisScreenPreview() {
    FinanceManagerTheme {
        AnalysisContent(
            uiState = mockTransactionUiStateLoading,
            onNavigateBack = { },
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalysisContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AnalysisContent(
    modifier: Modifier = Modifier,
    uiState: AnalysisUiState,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.analysis,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack,
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is AnalysisUiState.Loading -> LoadingProgressBar()
                is AnalysisUiState.Error -> {
                    /*ErrorContent(onRetry = onSave)*/
                }

                is AnalysisUiState.Success -> AnalysisForm(
                    uiState = uiState,

                    )
            }
        }
    }
}

@Composable
fun AnalysisForm(
    uiState: AnalysisUiState.Success,
) {

}
