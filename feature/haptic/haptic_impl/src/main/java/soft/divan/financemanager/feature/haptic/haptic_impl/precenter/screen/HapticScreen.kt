package soft.divan.financemanager.feature.haptic.haptic_impl.precenter.screen

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
import soft.divan.financemanager.feature.haptic.haptic_impl.R
import soft.divan.financemanager.feature.haptic.haptic_impl.precenter.model.HapticUiState
import soft.divan.financemanager.feature.haptic.haptic_impl.precenter.viewModel.HapticViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HapticScreenPreview() {
    FinanceManagerTheme {
        HapticContent(
            uiState = HapticUiState.Success(true),
            loadData = {},
            onNavigateBack = {},
            setHapticEnabled = {}
        )
    }
}

@Composable
fun HapticScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: HapticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HapticContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        loadData = viewModel::load,
        setHapticEnabled = viewModel::setHapticEnabled
    )
}

@Composable
fun HapticContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    uiState: HapticUiState,
    loadData: () -> Unit,
    setHapticEnabled: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.haptic,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack
                ),
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is HapticUiState.Error -> ErrorContent(onClick = { loadData() })
                is HapticUiState.Loading -> LoadingProgressBar()
                is HapticUiState.Success -> HapticSuccessContent(
                    modifier = modifier,
                    uiState = uiState,
                    setHapticEnabled = setHapticEnabled
                )
            }
        }
    }
}

@Composable
private fun HapticSuccessContent(
    modifier: Modifier = Modifier,
    uiState: HapticUiState.Success,
    setHapticEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = R.string.haptic), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = uiState.isEnabled,
            onCheckedChange = { setHapticEnabled(!uiState.isEnabled) }
        )
    }
}

