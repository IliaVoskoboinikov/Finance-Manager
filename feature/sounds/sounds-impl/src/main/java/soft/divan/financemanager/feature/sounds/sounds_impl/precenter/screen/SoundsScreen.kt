package soft.divan.financemanager.feature.sounds.sounds_impl.precenter.screen

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
import soft.divan.financemanager.feature.sounds.sounds_impl.R
import soft.divan.financemanager.feature.sounds.sounds_impl.precenter.model.SoundsUiState
import soft.divan.financemanager.feature.sounds.sounds_impl.precenter.viewModel.SoundViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SoundsScreenPreview() {
    FinanceManagerTheme {
        SoundsContent(
            uiState = SoundsUiState.Success(true),
            loadData = {},
            onNavigateBack = {},
            setSoundsEnabled = {}
        )
    }
}

@Composable
fun SoundsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: SoundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SoundsContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        loadData = viewModel::load,
        setSoundsEnabled = viewModel::setSoundsEnabled
    )
}

@Composable
fun SoundsContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    uiState: SoundsUiState,
    loadData: () -> Unit,
    setSoundsEnabled: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.sounds,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack
                ),
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is SoundsUiState.Error -> ErrorContent(onClick = { loadData() })
                is SoundsUiState.Loading -> LoadingProgressBar()
                is SoundsUiState.Success -> SoundsSuccessContent(
                    modifier = modifier,
                    uiState = uiState,
                    setSoundsEnabled = setSoundsEnabled
                )
            }
        }
    }
}

@Composable
private fun SoundsSuccessContent(
    modifier: Modifier = Modifier,
    uiState: SoundsUiState.Success,
    setSoundsEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = R.string.sounds), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = uiState.isEnabled,
            onCheckedChange = { setSoundsEnabled(!uiState.isEnabled) }
        )
    }
}

