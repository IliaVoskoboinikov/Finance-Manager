package soft.divan.financemanager.feature.transactionstoday.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.Period
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.transactions_today.impl.R
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.TransactionsTodayUiState
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TransactionsTodayViewModel @Inject constructor(
    private val getTransactionsByPeriodUseCase: GetTransactionsByPeriodUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    private val hapticsManager: HapticsManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TransactionsTodayUiState>(TransactionsTodayUiState.Loading)
    val uiState: StateFlow<TransactionsTodayUiState> = _uiState.asStateFlow()

    private var lastIsIncome: Boolean? = null

    // Держим ссылку на текущий сбор, чтобы повторный вызов не плодил параллельные
    // коллекторы (каждый launchIn создаёт новый) — отменяем предыдущий.
    private var loadJob: Job? = null

    fun loadTodayTransactions(isIncome: Boolean) {
        lastIsIncome = isIncome

        val today = Instant.now()
        loadJob?.cancel()
        loadJob = getTransactionsByPeriodUseCase(
            isIncome = isIncome,
            period = Period(today, today)
        )
            .onStart {
                _uiState.update { TransactionsTodayUiState.Loading }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { data ->
                        // Транзакции с неизвестной категорией пропускаем (рассинхрон/удалённая
                        // категория) — раньше `!!` ронял экран.
                        val uiTransactions = data.first.mapNotNull { transaction ->
                            data.third.find { it.id == transaction.categoryId }
                                ?.let { category -> transaction.toUi(category) }
                        }
                        val sumTransactions = getSumTransactionsUseCase(data.first)
                        _uiState.update {
                            TransactionsTodayUiState.Success(
                                transactions = uiTransactions,
                                sumTransaction = sumTransactions.toString() + " " + data.second.symbol
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            TransactionsTodayUiState.Error(
                                R.string.error_loading
                            )
                        }
                    }
                )
            }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    fun retry() {
        lastIsIncome?.let { loadTodayTransactions(it) }
    }

    fun hapticNavigation() {
        hapticsManager.perform(HapticType.CLICK)
    }
}
