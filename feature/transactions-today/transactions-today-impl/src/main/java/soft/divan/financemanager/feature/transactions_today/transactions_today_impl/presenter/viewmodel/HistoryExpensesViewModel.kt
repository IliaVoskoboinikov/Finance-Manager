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
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.mapper.formatWith
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.mapper.toUi
import soft.divan.financemanager.feature.expenses_income_shared.presenter.model.HistoryUiState
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTransactionsByPeriodUseCase
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryExpensesViewModel @Inject constructor(
    private val getTransactionsByPeriodUseCase: GetTransactionsByPeriodUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase

) : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()


    fun loadHistory(startDate: LocalDate, endDate: LocalDate) {

        getTransactionsByPeriodUseCase(
            isIncome = false,
            startDate = startDate,
            endDate = endDate
        )
            .onStart {
                _uiState.update { HistoryUiState.Loading }
            }
            .onEach { data ->
                val uiTransactions = data.first.map { transaction ->
                    transaction.toUi(
                        data.second,
                        data.third.find { it.id == transaction.categoryId }!!
                    )
                }
                val sumTransactions = getSumTransactionsUseCase.invoke(data.first)
                _uiState.update {
                    HistoryUiState.Success(
                        transactions = uiTransactions,
                        sumTransaction = sumTransactions.formatWith(data.second)
                    )
                }
            }
            .catch { exception ->
                _uiState.update { HistoryUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun updateStartDate(startDate: LocalDate) {
        val currentState = _uiState.value
        if (currentState is HistoryUiState.Success) {
            _uiState.value = currentState.copy(
                startDate = DateHelper.formatDateForDisplay(startDate)
            )
        }

    }

    fun updateEndDate(endDate: LocalDate) {
        val currentState = _uiState.value
        if (currentState is HistoryUiState.Success) {
            _uiState.value = currentState.copy(
                endDate = DateHelper.formatDateForDisplay(endDate)
            )
        }
    }

}