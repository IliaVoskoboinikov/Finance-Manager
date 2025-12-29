package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.feature.haptics.haptics_api.domain.HapticType
import soft.divan.financemanager.feature.haptics.haptics_api.domain.HapticsManager
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.R
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCase
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.TransactionsTodayUiState
import javax.inject.Inject

@HiltViewModel
class TransactionsTodayViewModel @Inject constructor(
    private val getTodayTransactionsUseCase: GetTodayTransactionsUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    private val hapticsManager: HapticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsTodayUiState>(TransactionsTodayUiState.Loading)
    val uiState: StateFlow<TransactionsTodayUiState> = _uiState.asStateFlow()

    fun loadTodayTransactions(isIncome: Boolean) {
        getTodayTransactionsUseCase(isIncome)
            .onStart {
                _uiState.update { TransactionsTodayUiState.Loading }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { data ->
                        val uiTransactions = data.first.map { transition ->
                            transition.toUi(
                                data.third.find { it.id == transition.categoryId }!!
                            )
                        }
                        val sumTransactions = getSumTransactionsUseCase(data.first)
                        _uiState.update {
                            TransactionsTodayUiState.Success(
                                transactions = uiTransactions,
                                sumTransaction = sumTransactions.toString() + " " + data.second.symbol
                            )
                        }
                    },
                    onFailure = { _uiState.update { TransactionsTodayUiState.Error(R.string.error_loading) } }
                )
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun hapticNavigation() {
        hapticsManager.perform(HapticType.CLICK)
    }
}