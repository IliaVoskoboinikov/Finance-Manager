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
import soft.divan.financemanager.domain.usecase.transaction.GetTodayExpensesUseCase
import soft.divan.financemanager.presenter.ui.model.ExpensesUiState
import javax.inject.Inject


@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getTodayExpensesUseCase: GetTodayExpensesUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadTodayExpenses()
    }

    private fun loadTodayExpenses() {
        getTodayExpensesUseCase.invoke()
            .onStart {
                _uiState.update { ExpensesUiState.Loading }
            }
            .onEach { data ->
                _uiState.update {
                    val sumTransactions = getSumTransactionsUseCase.invoke(data)
                    ExpensesUiState.Success(transactions = data, sumTransaction = sumTransactions.toPlainString()) }
            }
            .catch { exception ->
                _uiState.update { ExpensesUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }


}
