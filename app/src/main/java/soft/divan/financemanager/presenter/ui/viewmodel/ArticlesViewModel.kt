package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArticleUi(
    val emoji: String,
    val title: String
)

sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(val articleUis: List<ArticleUi>) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
}

class ArticlesViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val mockArticleUis = listOf(
        ArticleUi(emoji = "\uD83C\uDFE1", title = "Аренда квартиры"),
        ArticleUi(emoji = "\uD83D\uDC57", title = "Одежда"),
        ArticleUi(emoji = "\uD83D\uDC36", title = "На собачку"),
        ArticleUi(emoji = "\uD83D\uDC36", title = "На собачку"),
        ArticleUi(emoji = "pk", title = "Ремонт квартиры"),
        ArticleUi(emoji = "\uD83C\uDF6D", title = "Продукты"),
        ArticleUi(emoji = "\uD83C\uDFCB\uFE0F", title = "Спортзал"),
        ArticleUi(emoji = "\uD83D\uDC8A", title = "Медицина")
    )

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = ArticlesUiState.Success(mockArticleUis)
        }
    }

    fun search(query: String) {
        viewModelScope.launch(dispatcher) {
            val filtered = mockArticleUis.filter {
                it.title.contains(query, ignoreCase = true)
            }
            _uiState.value = ArticlesUiState.Success(filtered)
        }
    }
}
