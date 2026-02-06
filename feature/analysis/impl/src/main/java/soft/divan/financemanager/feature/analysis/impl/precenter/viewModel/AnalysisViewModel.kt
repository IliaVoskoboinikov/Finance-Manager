package soft.divan.financemanager.feature.analysis.impl.precenter.viewModel

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
import soft.divan.financemanager.core.domain.model.Period
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.analysis.impl.R
import soft.divan.financemanager.feature.analysis.impl.domain.usecase.GetCategoryPieChartDataUseCase
import soft.divan.financemanager.feature.analysis.impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.analysis.impl.precenter.mapper.toPieChartData
import soft.divan.financemanager.feature.analysis.impl.precenter.model.AnalysisUiState
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    private val getTransactionsByPeriodUseCase: GetTransactionsByPeriodUseCase,
    private val getCategoryPieChartDataUseCase: GetCategoryPieChartDataUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val isIncome: Boolean = savedStateHandle.get<Boolean>(IS_INCOME_KEY) ?: false

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(LocalDate.now())
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    init {
        observeDateChanges()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDateChanges() {
        combine(startDate, endDate) { start, end -> start to end }
            .onStart { _uiState.update { AnalysisUiState.Loading } }
            .flatMapLatest { (start, end) ->
                getTransactionsByPeriodUseCase(
                    isIncome = isIncome,
                    period = Period(
                        UiDateFormatter.formateOfDay(start),
                        UiDateFormatter.formateOfDay(end)
                    )
                )
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { data ->
                        val sum = getSumTransactionsUseCase(data.first)
                        val pie = getCategoryPieChartDataUseCase(data.first, data.third)

                        if (data.first.isEmpty()) {
                            _uiState.update { AnalysisUiState.EmptyData }
                        } else {
                            _uiState.update {
                                AnalysisUiState.Success(
                                    sumTransaction = "$sum ${data.second.symbol}",
                                    categoryPieSlice = pie.toPieChartData()
                                )
                            }
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            AnalysisUiState.Error(R.string.error_loading)
                        }
                    }
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

    fun reloadData() {
        _startDate.value = _startDate.value
        _endDate.value = _endDate.value
    }
}
// Revue me>>
