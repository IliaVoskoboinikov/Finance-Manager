package soft.divan.financemanager.presenter.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import soft.divan.financemanager.domain.util.DateHelper
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorSnackbar
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem

import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.SubContentTextListItem
import soft.divan.financemanager.uikit.icons.Arrow
import java.time.LocalDate


@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                LoadingProgressBar()
            }

            is HistoryUiState.Error -> {
                ErrorSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = uiState.message,
                )
            }

            is HistoryUiState.Success -> {
                val sortedItems = uiState.transactions.sortedByDescending { it.createdAt }
                Column(modifier = Modifier.padding(innerPadding)) {
                    ListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable(onClick = onStartDateClick),
                        content = { ContentTextListItem(stringResource(R.string.start)) },
                        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(startDate)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable(onClick = onEndDateClick),
                        content = { ContentTextListItem(stringResource(R.string.end)) },
                        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(endDate)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier.height(56.dp),
                        content = { ContentTextListItem(stringResource(R.string.all)) },
                        trail = { ContentTextListItem(uiState.sumTransaction) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    LazyColumn {
                        items(sortedItems) { item ->
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
                                    Column(horizontalAlignment = Alignment.End) {
                                        ContentTextListItem(text = item.amountFormatted)
                                        ContentTextListItem(DateHelper.formatDateTimeForDisplay(item.transactionDate))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Arrow,
                                        contentDescription = "arrow",
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                },
                            )
                            FMDriver()
                        }
                    }
                }
            }
        }
    }
}
