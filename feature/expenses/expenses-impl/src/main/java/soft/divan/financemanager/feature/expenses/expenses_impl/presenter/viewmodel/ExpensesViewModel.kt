package soft.divan.financemanager.feature.expenses.expenses_impl.presenter.viewmodel

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
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.mapper.formatWith
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.mapper.toUi
import soft.divan.financemanager.feature.expenses.expenses_impl.domain.GetTodayExpensesUseCase
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.mapper.ExpensesUiState
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getTodayExpensesUseCase: GetTodayExpensesUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase
) : ViewModel() {

    //todo
    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState
        .onStart { loadTodayExpenses() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ExpensesUiState.Loading
        )


    fun loadTodayExpenses() {
        getTodayExpensesUseCase()
            .onStart {
                _uiState.update { ExpensesUiState.Loading }
            }
            .onEach { data ->
                val uiTransactions = data.first.map { transition ->
                    transition.toUi(
                        data.second,
                        data.third.find { it.id == transition.categoryId }!!
                    )
                }
                val sumTransactions = getSumTransactionsUseCase.invoke(data.first)
                _uiState.update {
                    ExpensesUiState.Success(
                        transactions = uiTransactions,
                        sumTransaction = sumTransactions.formatWith(data.second)
                    )
                }
            }
            .catch { exception ->
                _uiState.update { ExpensesUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }


}