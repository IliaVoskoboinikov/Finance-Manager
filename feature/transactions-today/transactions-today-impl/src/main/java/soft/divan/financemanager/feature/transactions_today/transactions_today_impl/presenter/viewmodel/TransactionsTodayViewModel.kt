package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.viewmodel

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
import soft.divan.financemanager.core.domain.model.formatWith
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCase
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.TransactionsTodayUiState
import javax.inject.Inject

@HiltViewModel
class TransactionsTodayViewModel @Inject constructor(
    private val getTodayTransactionsUseCase: GetTodayTransactionsUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    //savedStateHandle: SavedStateHandle
) : ViewModel() {

    //private val isIncome: Boolean = savedStateHandle["isIncome"] ?: false

    private val _uiState = MutableStateFlow<TransactionsTodayUiState>(TransactionsTodayUiState.Loading)
    val uiState: StateFlow<TransactionsTodayUiState> = _uiState.asStateFlow()
    /* .onStart { loadTodayExpenses() }
     .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(5000L),
         ExpensesUiState.Loading
     )*/


    fun loadTodayTransactions(isIncome: Boolean) {
        getTodayTransactionsUseCase(isIncome)
            .onStart {
                _uiState.update { TransactionsTodayUiState.Loading }
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
                    TransactionsTodayUiState.Success(
                        transactions = uiTransactions,
                        sumTransaction = sumTransactions.formatWith(data.second)
                    )
                }
            }
            .catch { exception ->
                _uiState.update { TransactionsTodayUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }


}