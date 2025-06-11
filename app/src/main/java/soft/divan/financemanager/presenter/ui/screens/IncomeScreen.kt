package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.Clock
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.TopBar


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun IncomeScreenPreview() {
    IncomeScreen(navController = rememberNavController())
}


@Composable
fun IncomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        modifier = modifier,

        floatingActionButton = {
            FloatingButton(onClick = {})
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            Text("доходы")
        }
    }
}
