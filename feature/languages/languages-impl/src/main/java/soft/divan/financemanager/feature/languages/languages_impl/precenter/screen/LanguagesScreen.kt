package soft.divan.financemanager.feature.languages.languages_impl.precenter.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import soft.divan.financemanager.feature.languages.languages_impl.R
import soft.divan.financemanager.feature.languages.languages_impl.precenter.model.LanguageUi
import soft.divan.financemanager.feature.languages.languages_impl.precenter.model.LanguageUiState
import soft.divan.financemanager.feature.languages.languages_impl.precenter.viewModel.LanguagesViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun LanguagesScreenPreview() {
    FinanceManagerTheme {
        LanguagesContent(
            uiState = LanguageUiState.Success(LanguageUi.ENGLISH),
            onNavigateBack = {},
            onLanguageSelected = {}
        )
    }
}

@Composable
fun LanguagesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: LanguagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LanguagesContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onLanguageSelected = viewModel::onLanguageSelected
    )
}

@Composable
fun LanguagesContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    uiState: LanguageUiState,
    onLanguageSelected: (LanguageUi) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.languages,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack
                ),
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is LanguageUiState.Error -> ErrorContent(onClick = { })
                is LanguageUiState.Loading -> LoadingProgressBar()
                is LanguageUiState.Success -> LanguageSection(
                    selected = uiState.language,
                    onSelect = onLanguageSelected
                )
            }
        }
    }
}

@Composable
fun LanguageSection(
    modifier: Modifier = Modifier,
    selected: LanguageUi,
    onSelect: (LanguageUi) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.language_selection),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        LanguageItem(
            titleRes = LanguageUi.ENGLISH.title,
            selected = selected == LanguageUi.ENGLISH,
            onClick = { onSelect(LanguageUi.ENGLISH) }
        )
        LanguageItem(
            titleRes = LanguageUi.RUSSIAN.title,
            selected = selected == LanguageUi.RUSSIAN,
            onClick = { onSelect(LanguageUi.RUSSIAN) }
        )
    }
}


@Composable
fun LanguageItem(
    @StringRes titleRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}
