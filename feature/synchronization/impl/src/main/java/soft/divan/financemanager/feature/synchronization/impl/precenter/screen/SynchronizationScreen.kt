package soft.divan.financemanager.feature.synchronization.impl.precenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.synchronization.impl.R
import soft.divan.financemanager.feature.synchronization.impl.precenter.model.SynchronizationUiState
import soft.divan.financemanager.feature.synchronization.impl.precenter.viewModel.SynchronizationViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Suppress("MagicNumber")
@Composable
fun SynchronizationScreenPreview() {
    FinanceManagerTheme {
        SynchronizationContent(
            uiState = SynchronizationUiState.Success("12.12.25 14:00", 4),
            onNavigateBack = {},
            onIntervalChanged = {}
        )
    }
}

@Composable
fun SynchronizationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: SynchronizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SynchronizationContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onIntervalChanged = viewModel::onIntervalChanged
    )
}

@Composable
private fun SynchronizationContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    uiState: SynchronizationUiState,
    onIntervalChanged: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.synchronization,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is SynchronizationUiState.Error -> ErrorContent(onClick = { })

                is SynchronizationUiState.Loading -> LoadingProgressBar()

                is SynchronizationUiState.Success -> SynchronizationUiStateSuccess(
                    interval = uiState.hoursInterval,
                    lastSync = uiState.lastSyncTime ?: stringResource(
                        id = R.string.data_not_synced
                    ),
                    onIntervalChanged = onIntervalChanged

                )
            }
        }
    }
}

@Composable
private fun SynchronizationUiStateSuccess(
    modifier: Modifier = Modifier,
    interval: Int,
    lastSync: String,
    onIntervalChanged: (Int) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = pluralStringResource(
                id = R.plurals.sync_every_interval_hours,
                count = interval,
                interval
            ),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Slider(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = interval.toFloat(),
            onValueChange = { onIntervalChanged(it.toInt()) },
            valueRange = 1f..24f,
            steps = 23
        )

        Text(
            text = stringResource(id = R.string.last_sync, lastSync),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
// Revue me>>
