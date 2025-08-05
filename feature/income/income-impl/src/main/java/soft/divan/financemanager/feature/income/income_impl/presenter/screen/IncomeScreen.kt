package soft.divan.financemanager.feature.income.income_impl.presenter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.core.string.R
import soft.divan.financemanager.feature.income.income_impl.presenter.model.IncomeUiState
import soft.divan.financemanager.feature.income.income_impl.presenter.viewmodel.IncomeViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorSnackbar
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.FloatingButton
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.SubContentTextListItem
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.Clock
import soft.divan.financemanager.uikit.model.TopBarModel

@Composable
fun IncomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToHistory: () -> Unit,
    viewModel: IncomeViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                topBar = TopBarModel(
                    title = R.string.income_today,
                    actionIcon = Icons.Filled.Clock,
                    actionIconClick = { onNavigateToHistory() }
                ),
            )

            when (uiState) {
                is IncomeUiState.Loading -> {
                    LoadingProgressBar(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                is IncomeUiState.Error -> {
                    ErrorSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = (uiState as IncomeUiState.Error).message,
                    )
                }

                is IncomeUiState.Success -> {
                    val state = uiState as IncomeUiState.Success
                    Column {
                        ListItem(
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(),
                            content = { ContentTextListItem(stringResource(R.string.all)) },
                            trail = { ContentTextListItem(state.sumTransaction) },
                            containerColor = colorScheme.secondaryContainer
                        )
                        FMDriver()

                        LazyColumn {
                            items(state.transactions) { item ->
                                ListItem(
                                    modifier = Modifier.height(70.dp),
                                    lead = {
                                        EmojiCircle(emoji = item.category.emoji)
                                    },
                                    content = {
                                        Column {
                                            ContentTextListItem(item.category.name)
                                            if (!item.comment.isNullOrEmpty()) {
                                                SubContentTextListItem(item.comment)
                                            }
                                        }
                                    },
                                    trail = {
                                        ContentTextListItem("${item.amount} ₽")
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Arrow,
                                            contentDescription = "arrow",
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                                FMDriver()
                            }
                        }
                    }
                }
            }
        }

        FloatingButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
