package soft.divan.financemanager.feature.history.history_impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.feature.history.history_impl.R
import soft.divan.financemanager.feature.history.history_impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.history.history_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.history.history_impl.precenter.model.HistoryUiState
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTransactionsByPeriodUseCase: GetTransactionsByPeriodUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val isIncome: Boolean = savedStateHandle.get<Boolean>(IS_INCOME_KEY) ?: false

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _startDate = MutableStateFlow(DateHelper.getCurrentMonthStart())
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(DateHelper.getToday())
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    init {
        observeDateChanges()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDateChanges() {
        combine(startDate, endDate) { start, end -> start to end }
            .onStart { _uiState.update { HistoryUiState.Loading } }
            .flatMapLatest { (start, end) ->
                getTransactionsByPeriodUseCase(
                    isIncome = isIncome,
                    startDate = start,
                    endDate = end
                )
            }

            .onEach { result ->
                result.fold(
                    onSuccess = { data ->
                        val sumTransactions = getSumTransactionsUseCase(data.first)
                        val uiTransactions = data.first.map { transaction ->
                            transaction.toUi(data.third.find { it.id == transaction.categoryId }!!)
                        }

                        if (data.first.isEmpty()) {
                            _uiState.update { HistoryUiState.Error(R.string.empty) }
                        } else {
                            _uiState.update {
                                HistoryUiState.Success(
                                    transactions = uiTransactions,
                                    sumTransaction = sumTransactions.toString() + " " + data.second.symbol
                                )
                            }
                        }
                    },
                    onFailure = { _uiState.update { HistoryUiState.Error(R.string.error_loading) } }
                )
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun updateStartDate(date: LocalDate) {
        _startDate.value = date
    }

    fun updateEndDate(date: LocalDate) {
        _endDate.value = date
    }
}