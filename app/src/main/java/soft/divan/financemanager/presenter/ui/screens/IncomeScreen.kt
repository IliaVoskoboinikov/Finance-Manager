package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.icons.Clock
import soft.divan.financemanager.presenter.ui.icons.Diagram
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.IncomeListItemModel
import soft.divan.financemanager.presenter.ui.viewmodel.IncomeUiState
import soft.divan.financemanager.presenter.ui.viewmodel.IncomeViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.TopBar


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun IncomeScreenPreview() {
    FinanceManagerTheme {
        IncomeScreen(navController = rememberNavController())
    }
}

@Composable
fun IncomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: IncomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingButton(onClick = {})
        }
    ) { innerPadding ->

        when (state) {
            is IncomeUiState.Loading -> {}

            is IncomeUiState.Error -> {}

            is IncomeUiState.Success -> {
                val items = (state as IncomeUiState.Success).items
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(items) { item ->
                        RenderIncomeListItem(item)
                        FMDriver()
                    }
                }
            }

        }
    }
}


@Composable
fun RenderIncomeListItem(model: IncomeListItemModel) {
    when (model) {
        is IncomeListItemModel.Balance -> {
            ListItem(
                modifier = Modifier.height(56.dp),

                content = { ContentTextListItem(stringResource(model.content)) },
                trail = { ContentTextListItem(model.trail) },
                containerColor = colorScheme.secondaryContainer
            )
        }

        is IncomeListItemModel.Salary -> {
            ListItem(
                modifier = Modifier.height(70.dp),
                content = { ContentTextListItem(model.content) },
                trail = {
                    ContentTextListItem(model.prise)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
            )
        }
    }
}