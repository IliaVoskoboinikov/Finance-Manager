package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.icons.Diagram
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.AccountItem
import soft.divan.financemanager.presenter.ui.viewmodel.AccountUiState
import soft.divan.financemanager.presenter.ui.viewmodel.AccountViewModel


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountScreenPreview() {
    FinanceManagerTheme {
        AccountScreen(navController = rememberNavController())
    }
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AccountViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = { FloatingButton(onClick = {}) }
    ) { innerPadding ->
        when (state) {
            is AccountUiState.Loading -> {

            }

            is AccountUiState.Error -> {

            }

            is AccountUiState.Success -> {
                val items = (state as AccountUiState.Success).items

                Column(modifier = Modifier.padding(innerPadding)) {
                    LazyColumn {
                        items(items) { item ->
                            RenderAccountListItem(item)
                            if (item != items.last()) {
                                FMDriver()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Icon(
                        imageVector = Icons.Filled.Diagram,
                        contentDescription = "Diagram",
                        modifier = Modifier.fillMaxWidth(),
                        tint = Color.Unspecified
                    )


                }
            }
        }
    }
}


@Composable
fun RenderAccountListItem(item: AccountItem) {
    when (item) {
        is AccountItem.Balance -> {
            ListItem(
                modifier = Modifier.height(56.dp),
                lead = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, textAlign = TextAlign.Center)
                    }
                },
                content = { ContentTextListItem(item.label) },
                trail = {
                    ContentTextListItem(item.amount)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                containerColor = colorScheme.secondaryContainer
            )
        }

        is AccountItem.Currency -> {
            ListItem(
                modifier = Modifier.height(56.dp),
                content = { ContentTextListItem(item.label) },
                trail = {
                    ContentTextListItem(item.symbol)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                containerColor = colorScheme.secondaryContainer
            )
        }
    }
}
