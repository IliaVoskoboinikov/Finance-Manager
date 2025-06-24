package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import soft.divan.financemanager.R
import javax.inject.Inject


sealed class IncomeListItemUiModel {
    data class Balance(
        val content: Int,
        val trail: String,
    ) : IncomeListItemUiModel()

    data class Salary(
        val content: String,
        val prise: String,
        val onClick: () -> Unit
    ) : IncomeListItemUiModel()
}


sealed class IncomeUiState {
    data object Loading : IncomeUiState()
    data class Success(val items: List<IncomeListItemUiModel>) : IncomeUiState()
    data class Error(val message: String) : IncomeUiState()
}

@HiltViewModel
class IncomeViewModel @Inject constructor( ) : ViewModel() {

    private val _uiState = MutableStateFlow<IncomeUiState>(IncomeUiState.Loading)
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadIncomeItems()
    }

    private val mockIncome = listOf(
        IncomeListItemUiModel.Balance(
            content = R.string.all,
            trail = "600 000 ₽",
        ),
        IncomeListItemUiModel.Salary(
            content = "Зарплата",
            prise = "500 000₽",
            onClick = {}
        ),
        IncomeListItemUiModel.Salary(
            content = "Подработка",
            prise = "100 000₽",
            onClick = {}
        ),
    )

    private fun loadIncomeItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = IncomeUiState.Success(mockIncome)
        }
    }
}
