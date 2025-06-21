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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.icons.Diagram
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.AccountViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountScreenPreview() {
    FinanceManagerTheme {
        AccountContent(uiState = AccountUiState.Loading, navController = rememberNavController())
    }
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountContent(modifier = modifier, uiState = uiState, navController = navController)
}

@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { FloatingButton(onClick = { navController.navigate(AddAccountScreen.route) }) }
    ) { innerPadding ->
        when (uiState) {
            is AccountUiState.Loading -> {
                LoadingProgressBar()
            }

            is AccountUiState.Error -> {
                ErrorSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = uiState.message,
                )
            }

            is AccountUiState.Success -> {

                Column(modifier = Modifier.padding(innerPadding)) {

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
                                Text(text = "\uD83D\uDCB0", textAlign = TextAlign.Center)
                            }
                        },
                        content = { ContentTextListItem(uiState.account.name) },
                        trail = {
                            ContentTextListItem(uiState.account.balance.toPlainString())
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Filled.Arrow,
                                contentDescription = "arrow",
                                tint = colorScheme.onSurfaceVariant
                            )
                        },
                        containerColor = colorScheme.secondaryContainer
                    )

                    FMDriver()


                    ListItem(
                        modifier = Modifier.height(56.dp),
                        content = { ContentTextListItem("Валюта") },
                        trail = {
                            ContentTextListItem(uiState.account.currency)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Filled.Arrow,
                                contentDescription = "arrow",
                                tint = colorScheme.onSurfaceVariant
                            )
                        },
                        containerColor = colorScheme.secondaryContainer
                    )


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
