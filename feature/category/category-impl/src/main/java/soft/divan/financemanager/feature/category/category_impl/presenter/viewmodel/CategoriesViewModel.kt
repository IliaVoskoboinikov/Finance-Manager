package soft.divan.financemanager.feature.category.category_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.CategoriesUiState
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.toDomain
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.toUi
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
            SharingStarted.Companion.WhileSubscribed(5000L),
            CategoriesUiState.Loading
        )

    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            getCategoriesUseCase.invoke()
                .onStart {
                    _uiState.update { CategoriesUiState.Loading }
                }
                .onEach { data ->
                    val categories = data.map { it.toUi() }
                    _uiState.value =
                        CategoriesUiState.Success(categories = categories, sortedCategories = categories)

                }
                .catch { exception ->
                    _uiState.update { CategoriesUiState.Error(exception.message.toString()) }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(viewModelScope)
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