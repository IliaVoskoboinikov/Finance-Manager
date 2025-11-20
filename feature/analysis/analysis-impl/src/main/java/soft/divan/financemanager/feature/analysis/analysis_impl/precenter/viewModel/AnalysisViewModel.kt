package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.viewModel

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
import soft.divan.financemanager.core.domain.usecase.GetTransactionByPeriodUseCase
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.feature.analysis.analysis_impl.domain.usecase.GetCategoryPieChartDataUseCase
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.mapper.toPieChartData
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.AnalysisUiState
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    private val getTransactionByPeriodUseCase: GetTransactionByPeriodUseCase,
    private val getCategoryPieChartDataUseCase: GetCategoryPieChartDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    //todo
    var isIncome = false

    fun setIsIncome(isIncome: Boolean) {
        this.isIncome = isIncome
    }

    //todo
    fun load(startDate: LocalDate, endDate: LocalDate) {
        getTransactionByPeriodUseCase(
            isIncome = isIncome,
            startDate = startDate,
            endDate = endDate
        )
            .onStart {
                _uiState.update { AnalysisUiState.Loading }
            }
            .onEach { data ->

            val sumTransactions = getSumTransactionsUseCase(data.first)

                val categoryPieSlice = getCategoryPieChartDataUseCase(data.first, data.third)

                if (!data.first.isEmpty()) {
                    _uiState.update {
                        AnalysisUiState.Success(
                            sumTransaction = sumTransactions.formatWith(data.second),
                            categoryPieSlice = categoryPieSlice.toPieChartData()
                        )
                    }
                } else {
                    _uiState.update { AnalysisUiState.Error("empty") }
                }
            }
            .catch { exception ->
                _uiState.update { AnalysisUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }


    fun updateStartDate(startDate: LocalDate) {
        val currentState = _uiState.value
        if (currentState is AnalysisUiState.Success) {
            _uiState.value = currentState.copy(
                startDate = DateHelper.formatDateForDisplay(startDate)
            )
        }

    }

    fun updateEndDate(endDate: LocalDate) {
        val currentState = _uiState.value
        if (currentState is AnalysisUiState.Success) {
            _uiState.value = currentState.copy(
                endDate = DateHelper.formatDateForDisplay(endDate)
            )
        }
    }

}