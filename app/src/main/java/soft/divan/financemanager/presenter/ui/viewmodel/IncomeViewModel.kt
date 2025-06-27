package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.domain.usecase.transaction.GetSumTransactionsUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetTodayIncomeUseCase
import soft.divan.financemanager.presenter.ui.model.IncomeUiState
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val getTodayIncomeUseCase: GetTodayIncomeUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IncomeUiState>(IncomeUiState.Loading)
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayIncome()
    }

    private fun loadTodayIncome() {
        getTodayIncomeUseCase()
            .onStart {
                _uiState.update { IncomeUiState.Loading }
            }
            .onEach { transactions ->
                val sum = getSumTransactionsUseCase(transactions)
                _uiState.update {
                    IncomeUiState.Success(
                        transactions = transactions,
                        sumTransaction = sum.toPlainString()
                    )
                }
            }
            .catch { exception ->
                _uiState.update { IncomeUiState.Error(exception.message ?: "Unknown error") }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
}
