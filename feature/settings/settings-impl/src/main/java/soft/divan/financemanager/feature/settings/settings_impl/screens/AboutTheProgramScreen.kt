package soft.divan.financemanager.feature.settings.settings_impl.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import soft.divan.financemanager.feature.settings.settings_impl.R
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel

@Composable
fun AboutTheProgramScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {


    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {


            TopBar(
                topBar = TopBarModel(title = R.string.settings),
            )


            Text(text = stringResource(R.string.about_the_program))

        }
    }
}