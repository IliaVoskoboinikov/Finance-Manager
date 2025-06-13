package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import soft.divan.financemanager.presenter.ui.icons.Search
import soft.divan.financemanager.presenter.ui.viewmodel.Article
import soft.divan.financemanager.presenter.ui.viewmodel.ArticlesUiState
import soft.divan.financemanager.presenter.ui.viewmodel.ArticlesViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.ListItem

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ArticlesScreenPreview() {
    ArticlesScreen(navController = rememberNavController())
}
@Composable
fun ArticlesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ArticlesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(modifier = modifier) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.search(it)
                    },
                    onSearchClick = { viewModel.search(query) }
                )
            }

            item { FMDriver() }

            when (uiState) {
                is ArticlesUiState.Loading -> {

                }

                is ArticlesUiState.Success -> {
                    val articles = (uiState as ArticlesUiState.Success).articles
                    items(articles) { article ->
                        RenderArticleListItem(article)
                        FMDriver()
                    }
                }

                is ArticlesUiState.Error -> {

                }
            }
        }
    }
}

@Composable
fun RenderArticleListItem(article: Article) {
    ListItem(
        modifier = Modifier.height(70.dp),
        lead = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = article.emoji, textAlign = TextAlign.Center)
            }
        },
        content = { ContentTextListItem(article.title) }
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)

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
