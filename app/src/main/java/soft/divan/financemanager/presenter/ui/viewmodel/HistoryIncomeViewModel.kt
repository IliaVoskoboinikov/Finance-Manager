package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import soft.divan.financemanager.domain.usecase.transaction.GetIncomeByPeriodUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetSumTransactionsUseCase
import soft.divan.financemanager.domain.util.DateHelper
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryIncomeViewModel @Inject constructor(
    private val getIncomeByPeriodUseCase: GetIncomeByPeriodUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory(startDate: LocalDate, endDate: LocalDate) {
        getIncomeByPeriodUseCase(startDate, endDate)
            .onStart {
                _uiState.value = HistoryUiState.Loading
            }
            .onEach { data ->
                val sum = getSumTransactionsUseCase(data)
                _uiState.value = HistoryUiState.Success(
                    transactions = data,
                    sumTransaction = sum.toPlainString()
                )
            }
            .catch { error ->
                _uiState.value = HistoryUiState.Error(error.message ?: "Ошибка загрузки")
            }
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
                endDate = DateHelper.formatDateForDisplay(endDate)!!
            )
        }
    }
}
