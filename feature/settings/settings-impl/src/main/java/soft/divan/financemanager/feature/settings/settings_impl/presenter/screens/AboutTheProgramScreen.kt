package soft.divan.financemanager.feature.settings.settings_impl.presenter.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.feature.settings.settings_impl.R
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AboutTheProgramScreenPreview() {
    FinanceManagerTheme {
        AboutTheProgramScreen()
    }
}

@Composable
fun AboutTheProgramScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TopBar(topBar = TopBarModel(title = R.string.settings)) },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = stringResource(R.string.about_the_program))
        }
    }
}