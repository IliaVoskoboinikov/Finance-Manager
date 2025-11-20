package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase

import soft.divan.financemanager.feature.transaction.transaction_impl.R
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesExpensesUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiCategory
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiTransaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsUseCase: GetTransactionUseCase,
    private val getSumTransactionsUseCase: GetSumTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesExpensesUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase

) : ViewModel() {
    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Loading)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TransactionEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun createTransaction() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                _uiState.update { TransactionUiState.Loading }
                //todo удалить invoke
                createTransactionUseCase.invoke(transaction = currentState.transaction.toDomain())
                    .fold(
                        onSuccess = { _eventFlow.emit(TransactionEvent.TransactionDeleted) },
                        onFailure = {
                            _eventFlow.emit(TransactionEvent.ShowError(R.string.error_save))
                            _uiState.update { currentState }
                        }
                    )
            }
        }
    }

    fun load(transactionId: Int?, isIncome: Boolean?) {
        viewModelScope.launch {
            _uiState.update { TransactionUiState.Loading }

            val categoriesResult = getCategoriesUseCase().first()
            val accountsResult = getAccountsUseCase().first()

            if (transactionId == null) {
                crateNewTransaction(accountsResult, categoriesResult)
            } else {
                loadOldTransaction(transactionId, categoriesResult, accountsResult)
            }
        }
    }

    private fun crateNewTransaction(
        accountsResult: List<Account>,
        categoriesResult: List<Category>
    ) {
        //todo !!!
        val accountId = accountsResult.firstOrNull()?.id
        val accountName = accountsResult.firstOrNull()?.name
        val category = categoriesResult.firstOrNull()?.toUi() ?: UiCategory(1, "empyt", "empyt", false)
        val now = LocalDateTime.now()
        _uiState.value = TransactionUiState.Success(
            transaction = UiTransaction(
                id = -1,
                accountId = accountId ?: 1,
                category = category,
                amount = BigDecimal.ZERO,
                transactionDate = now,
                comment = "",
                createdAt = now,
                updatedAt = now,
                amountFormatted = BigDecimal.ZERO.toString(),
            ),
            categories = categoriesResult.map {
                it.toUi()
            },
            accountName = accountName ?: " empty"
        )
    }

    private suspend fun loadOldTransaction(
        transactionId: Int,
        categoriesResult: List<Category>,
        accountsResult: List<Account>
    ) {
        //todo fold
        val result = getTransactionsUseCase(transactionId)
        if (result.isSuccess) {
            val transaction = result.getOrThrow()
            _uiState.update {
                TransactionUiState.Success(
                    //todo
                    transaction = transaction.toUi(
                        currency = CurrencyCode("RUB"),
                        category = categoriesResult.find { it.id == transaction.categoryId }!!
                    ),
                    categories = categoriesResult.map { it.toUi() },
                    accountName = accountsResult.first().name
                )
            }
        } else {
            //todo
            _uiState.value = TransactionUiState.Error("Не удалось загрузить транзакцию")
        }
    }

    fun updateComment(comment: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                _uiState.update {
                    currentState.copy(
                        transaction = currentState.transaction.copy(
                            comment = comment
                        )
                    )
                }

            }
        }
    }

    fun updateDate(date: LocalDate) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                val oldTransaction = currentState.transaction
                // сохраняем время, чтобы не сбрасывалось при смене даты
                val newDateTime = LocalDateTime.of(date, oldTransaction.transactionDate.toLocalTime())

                _uiState.update {
                    currentState.copy(
                        transaction = oldTransaction.copy(
                            transactionDate = newDateTime,
                            updatedAt = LocalDateTime.now()
                        )
                    )
                }
            }
        }
    }

    fun updateTime(time: LocalTime) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                val oldTransaction = currentState.transaction
                val newDateTime = LocalDateTime.of(oldTransaction.transactionDate.toLocalDate(), time)

                _uiState.update {
                    currentState.copy(
                        transaction = oldTransaction.copy(
                            transactionDate = newDateTime,
                            updatedAt = LocalDateTime.now()
                        )
                    )
                }
            }
        }
    }

    fun updateAmount(amount: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                _uiState.update {
                    currentState.copy(
                        transaction = currentState.transaction.copy(
                            amount = amount.toBigDecimal()
                        )
                    )
                }

            }
        }
    }

    fun updateCategory(category: UiCategory) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                _uiState.update {
                    currentState.copy(
                        transaction = currentState.transaction.copy(
                            category = category
                        )
                    )
                }
            }
        }
    }

    fun delete(idTransaction: Int?) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (idTransaction != null && currentState is TransactionUiState.Success) {
                deleteTransactionUseCase.invoke(idTransaction).fold(
                    onSuccess = { _eventFlow.emit(TransactionEvent.TransactionDeleted) },
                    onFailure = {
                        _eventFlow.emit(TransactionEvent.ShowError(R.string.fail_delete_transaction))
                        _uiState.update { currentState }
                    }
                )
            }
        }
    }
}
