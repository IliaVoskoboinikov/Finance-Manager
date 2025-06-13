package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Article(
    val emoji: String,
    val title: String
)

sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(val articles: List<Article>) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
}

class ArticlesViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val mockArticles = listOf(
        Article(emoji = "\uD83C\uDFE1", title = "Аренда квартиры"),
        Article(emoji = "\uD83D\uDC57", title = "Одежда"),
        Article(emoji = "\uD83D\uDC36", title = "На собачку"),
        Article(emoji = "\uD83D\uDC36", title = "На собачку"),
        Article(emoji = "pk", title = "Ремонт квартиры"),
        Article(emoji = "\uD83C\uDF6D", title = "Продукты"),
        Article(emoji = "\uD83C\uDFCB\uFE0F", title = "Спортзал"),
        Article(emoji = "\uD83D\uDC8A", title = "Медицина")
    )

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = ArticlesUiState.Success(mockArticles)
        }
    }

    fun search(query: String) {
        viewModelScope.launch(dispatcher) {
            val filtered = mockArticles.filter {
                it.title.contains(query, ignoreCase = true)
            }
            _uiState.value = ArticlesUiState.Success(filtered)
        }
    }
}
