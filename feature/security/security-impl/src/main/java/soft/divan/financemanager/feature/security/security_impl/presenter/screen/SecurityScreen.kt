package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.security_impl.R
import soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel.SecurityViewModel
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true)
@Composable
fun PreviewSecurityScreen() {

    FinanceManagerTheme {
        SecurityScreen(onNavigateBack = {}, onNavigateToCreatePin = {})
    }
}


@Composable
fun SecurityScreen(
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCreatePin: () -> Unit
) {

    val pin = viewModel.pin.collectAsStateWithLifecycle()
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                topBar = TopBarModel(
                    title = R.string.security,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = { onNavigateBack() },
                )
            )

            Spacer(modifier = Modifier.size(50.dp))

            Button(
                onClick = { onNavigateToCreatePin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Text(text = stringResource(R.string.create_pin))
            }

            Spacer(modifier = Modifier.size(50.dp))

            if (!pin.value.isEmpty())
                Button(
                    onClick = { viewModel.deletePin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text(text = stringResource(R.string.delete_pin))
                }
        }


    }
}
