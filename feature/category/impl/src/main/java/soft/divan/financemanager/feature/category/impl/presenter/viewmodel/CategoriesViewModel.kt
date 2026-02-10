package soft.divan.financemanager.feature.category.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.feature.category.impl.R
import soft.divan.financemanager.feature.category.impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.impl.presenter.mapper.toDomain
import soft.divan.financemanager.feature.category.impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.category.impl.presenter.model.CategoriesUiState
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
            SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            CategoriesUiState.Loading
        )

    fun loadCategories() {
        getCategoriesUseCase()
            .onStart { _uiState.update { CategoriesUiState.Loading } }
            .onEach { result ->
                result.fold(
                    onSuccess = { categories ->
                        val categories = categories.map { it.toUi() }
                        if (categories.isEmpty()) {
                            _uiState.update { CategoriesUiState.EmptyData }
                        } else {
                            _uiState.update {
                                CategoriesUiState.Success(
                                    categories = categories,
                                    filteredCategories = categories
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        if (error == DomainError.NoData) {
                            _uiState.update { CategoriesUiState.EmptyData }
                        }
                        _uiState.update { CategoriesUiState.Error(R.string.error_loading) }
                    }
                )
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
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
// Revue me>>
