package soft.divan.financemanager.feature.category.category_impl.presenter.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.feature.category.category_impl.R
import soft.divan.financemanager.feature.category.category_impl.presenter.model.CategoriesUiState
import soft.divan.financemanager.feature.category.category_impl.presenter.model.UiCategory
import soft.divan.financemanager.feature.category.category_impl.presenter.viewmodel.CategoriesViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Search
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CategoriesScreenPreview() {
    FinanceManagerTheme {
        CategoriesScreen(navController = rememberNavController())
    }
}

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CategoriesViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesContent(
        modifier = modifier,
        uiState = uiState,
        onSearch = viewModel::search,
        onRetry = viewModel::retry
    )
}

@Composable
private fun CategoriesContent(
    modifier: Modifier,
    uiState: CategoriesUiState,
    onSearch: (String) -> Unit,
    onRetry: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    Column(modifier = modifier) {

        TopBar(
            topBar = TopBarModel(
                title = R.string.my_articles
            )
        )


        SearchBar(
            query = query,
            onQueryChange = {
                query = it
                onSearch(it)
            },
            onSearchClick = { onSearch(query) }
        )
        FMDriver()
        when (uiState) {
            is CategoriesUiState.Loading -> {
                LoadingProgressBar()
            }

            is CategoriesUiState.Error -> {
                ErrorContent(onRetry = { onRetry() })
            }

            is CategoriesUiState.Success -> {
                val categories = uiState.sortedCategories
                if (categories.isEmpty()) {
                    Text(
                        text = stringResource(R.string.it_seems_nothing_found),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn {
                        items(categories) {
                            RenderArticleListItem(it)
                            FMDriver()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderArticleListItem(categoryUiModel: UiCategory) {
    ListItem(
        modifier = Modifier.height(70.dp),
        lead = {
            EmojiCircle(emoji = categoryUiModel.emoji)
        },
        content = { ContentTextListItem(categoryUiModel.name) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(colorScheme.surfaceContainerHigh)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)

        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    ContentTextListItem(
                        text = stringResource(R.string.find_article),
                        color = colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledSupportingTextColor = Color.Transparent,
                ),
            )

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Black,
                    modifier = Modifier.size(48.dp)
                )
            }

        }
    }
}
