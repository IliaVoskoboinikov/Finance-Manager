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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.domain.usecase.category.GetCategoriesUseCase
import soft.divan.financemanager.domain.usecase.category.SearchCategoryUseCase
import soft.divan.financemanager.domain.util.resolve
import soft.divan.financemanager.presenter.mapper.toDomain
import soft.divan.financemanager.presenter.mapper.toUi
import soft.divan.financemanager.presenter.ui.model.CategoriesUiState
import soft.divan.financemanager.presenter.ui.model.CategoryUiModel
import javax.inject.Inject


@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchCategoryUseCase: SearchCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState
        .onStart { loadCategories() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            CategoriesUiState.Loading
        )

    private val mockArticleUiModels = listOf(
        CategoryUiModel(emoji = "\uD83C\uDFE1", title = "Аренда квартиры"),
        CategoryUiModel(emoji = "\uD83D\uDC57", title = "Одежда"),
        CategoryUiModel(emoji = "\uD83D\uDC36", title = "На собачку"),
        CategoryUiModel(emoji = "\uD83D\uDC36", title = "На собачку"),
        CategoryUiModel(emoji = "pk", title = "Ремонт квартиры"),
        CategoryUiModel(emoji = "\uD83C\uDF6D", title = "Продукты"),
        CategoryUiModel(emoji = "\uD83C\uDFCB\uFE0F", title = "Спортзал"),
        CategoryUiModel(emoji = "\uD83D\uDC8A", title = "Медицина")
    )


    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            getCategoriesUseCase.invoke().resolve(
                onSuccess = { data ->
                    val categories = data.map { it.toUi() }
                    _uiState.value =
                        CategoriesUiState.Success(categories = categories, sortedCategories = categories)
                },
                onError = { throwable ->
                    _uiState.update { CategoriesUiState.Error(throwable.message.toString()) }
                }
            )
        }
    }

    fun retry() {
        _uiState.update { CategoriesUiState.Loading }
        loadCategories()

    }

    fun search(query: String) {
        val currentState = uiState.value
        //todo
        if (currentState !is CategoriesUiState.Success) return

        val categories = currentState.categories

        viewModelScope.launch(Dispatchers.Default) {
            val sortedCategories = searchCategoryUseCase(
                query = query,
                categories = categories.map { it.toDomain() }
            )

            _uiState.update {
                CategoriesUiState.Success(
                    categories = categories,
                    sortedCategories = sortedCategories.map { it.toUi() }
                )
            }
        }
    }

}
