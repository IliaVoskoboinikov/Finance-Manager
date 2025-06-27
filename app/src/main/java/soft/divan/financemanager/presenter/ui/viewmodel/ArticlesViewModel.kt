package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ArticleUi(
    val emoji: String,
    val title: String
)

sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(val articleUis: List<ArticleUi>) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
}

@HiltViewModel
class ArticlesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)
    val uiState: StateFlow<ArticlesUiState> = _uiState
        .onStart { loadArticles() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ArticlesUiState.Loading
        )

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


    private fun loadArticles() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ArticlesUiState.Success(mockArticleUis)
        }
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val filtered = mockArticleUis.filter {
                it.title.contains(query, ignoreCase = true)
            }
            _uiState.value = ArticlesUiState.Success(filtered)
        }
    }
}
