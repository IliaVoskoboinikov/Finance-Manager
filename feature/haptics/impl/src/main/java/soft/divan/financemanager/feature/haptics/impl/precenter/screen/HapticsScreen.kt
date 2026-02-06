package soft.divan.financemanager.feature.haptics.impl.precenter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.haptics.impl.R
import soft.divan.financemanager.feature.haptics.impl.precenter.model.HapticsUiState
import soft.divan.financemanager.feature.haptics.impl.precenter.viewModel.HapticsViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HapticsScreenPreview() {
    FinanceManagerTheme {
        HapticsContent(
            uiState = HapticsUiState.Success(true),
            loadData = {},
            onNavigateBack = {},
            setHapticEnabled = {}
        )
    }
}

@Composable
fun HapticsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: HapticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HapticsContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        loadData = viewModel::load,
        setHapticEnabled = viewModel::setHapticEnabled
    )
}

@Composable
private fun HapticsContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    uiState: HapticsUiState,
    loadData: () -> Unit,
    setHapticEnabled: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.haptics,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is HapticsUiState.Error -> ErrorContent(onClick = { loadData() })

                is HapticsUiState.Loading -> LoadingProgressBar()

                is HapticsUiState.Success -> HapticsSuccessContent(
                    modifier = modifier,
                    uiState = uiState,
                    setHapticEnabled = setHapticEnabled
                )
            }
        }
    }
}

@Composable
private fun HapticsSuccessContent(
    modifier: Modifier = Modifier,
    uiState: HapticsUiState.Success,
    setHapticEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.haptics),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = uiState.isEnabled,
            onCheckedChange = { setHapticEnabled(!uiState.isEnabled) }
        )
    }
}
// Revue me>>
