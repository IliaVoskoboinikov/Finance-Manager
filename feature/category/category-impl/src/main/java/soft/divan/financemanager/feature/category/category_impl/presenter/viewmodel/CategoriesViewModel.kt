package soft.divan.financemanager.feature.category.category_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.feature.category.category_impl.R
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.category_impl.presenter.mapper.toDomain
import soft.divan.financemanager.feature.category.category_impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.category.category_impl.presenter.model.CategoriesUiState
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

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .onStart {
                    _uiState.update { CategoriesUiState.Loading }
                }
                .catch {
                    _uiState.update { CategoriesUiState.Error(R.string.error_loading) }
                }
                .collect { data ->
                    val categories = data.map { it.toUi() }
                    _uiState.update {
                        CategoriesUiState.Success(
                            categories = categories,
                            filteredCategories = categories
                        )
                    }
                }
        }
    }

    fun retry() {
        loadCategories()
    }

    fun search(query: String) {
        val currentState = uiState.value

        if (currentState !is CategoriesUiState.Success) return

        val categories = currentState.categories

        viewModelScope.launch {
            val sortedCategories = searchCategoryUseCase(
                query = query,
                categories = categories.map { it.toDomain() }
            )

            _uiState.update {
                CategoriesUiState.Success(
                    categories = categories,
                    filteredCategories = sortedCategories.map { it.toUi() }
                )
            }
        }
    }

}