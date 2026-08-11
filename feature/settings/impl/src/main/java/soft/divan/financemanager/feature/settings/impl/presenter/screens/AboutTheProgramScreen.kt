package soft.divan.financemanager.feature.settings.impl.presenter.screens

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
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.feature.settings.api.AboutTheProgramKey
import soft.divan.financemanager.feature.settings.impl.R
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@NavPreview(route = AboutTheProgramKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AboutTheProgramScreenPreview() {
    FinanceManagerTheme {
        AboutTheProgramScreen()
    }
}

@NavDestination(route = AboutTheProgramKey::class)
@Composable
fun AboutTheProgramScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopBar(topBar = TopBarModel(title = R.string.settings)) }
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
