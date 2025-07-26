package soft.divan.financemanager.feature.settings.settings_impl.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import soft.divan.financemanager.feature.settings.settings_impl.viewModel.CollorSelectionViewModel
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel

@Composable
fun AboutTheProgramScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CollorSelectionViewModel = hiltViewModel()
) {


    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {


            TopBar(
                topBar = TopBarModel(title = soft.divan.financemanager.string.R.string.settings),
            )


            Text(text = "About the program")

        }
    }
}