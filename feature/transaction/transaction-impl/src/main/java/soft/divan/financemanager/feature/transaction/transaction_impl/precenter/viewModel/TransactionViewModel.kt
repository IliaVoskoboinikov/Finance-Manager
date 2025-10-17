package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.util.resolve
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiCategory
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.toDomain
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.toUi
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesExpensesUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
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
    private val getCategoriesUseCase: GetCategoriesExpensesUseCase

) : ViewModel() {
    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Loading)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()


    fun createTransaction() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success) {
                _uiState.update { TransactionUiState.Loading }
                createTransactionUseCase.invoke(transaction = currentState.transaction.toDomain())
                    .resolve(
                        onSuccess = { _uiState.update { currentState } },
                        onError = { _uiState.update { TransactionUiState.Error("") } }
                    )
            }
        }
    }

    fun load(transactionId: Int?, isIncome: Boolean) {
        viewModelScope.launch {
            _uiState.update { TransactionUiState.Loading }

            val categoriesResult = getCategoriesUseCase().first()
            val accountsResult = getAccountsUseCase().first()




            if (transactionId == null) {
                val now = LocalDateTime.now()
                _uiState.value = TransactionUiState.Success(
                    transaction = UiTransaction(
                        id = -1,
                        accountId = accountsResult.first().id,
                        category = categoriesResult.first().toUi(),
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
                    accountName = accountsResult.first().name
                )
            } else {
                /* val result = getTransactionUseCase(transactionId)
                 if (result.isSuccess) {
                     val transaction = result.getOrThrow()
                     _uiState.value = TransactionUiState.Success(
                         transaction = transaction,
                         categories = categories,
                         accounts = cachedAccounts,
                         isEditMode = true
                     )
                 } else {
                     _uiState.value = TransactionUiState.Error("Не удалось загрузить транзакцию")
                 }*/
            }
        }
    }

    /*  fun updateStartDate(startDate: LocalDate) {
          val currentState = _uiState.value
          if (currentState is TransactionUiState.Success) {
              _uiState.update { currentState.copy(transaction = currentState.transaction.copy()) }
          }

      }*/

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
}