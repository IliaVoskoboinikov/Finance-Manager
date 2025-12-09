package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.feature.transaction.transaction_impl.R
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.UpdateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.transaction.transaction_impl.navigation.TRANSACTION_ID_KEY
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUiState
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiCategory
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesByTypeUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Int? = savedStateHandle.get<Int>(TRANSACTION_ID_KEY)
    private val isIncome: Boolean = savedStateHandle.get<Boolean>(IS_INCOME_KEY) ?: false

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Loading)
    val uiState: StateFlow<TransactionUiState> = _uiState.onStart { load() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            TransactionUiState.Loading
        )

    private val _eventFlow = MutableSharedFlow<TransactionEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val mode =
        if (transactionId == null) TransactionMode.Create else TransactionMode.Edit(transactionId)

    fun load() {
        viewModelScope.launch {
            _uiState.update { TransactionUiState.Loading }
            val categories = getCategoriesUseCase(isIncome).first()
            val accounts = getAccountsUseCase().first()
            when (mode) {
                is TransactionMode.Create -> loadForCreate(accounts, categories)
                is TransactionMode.Edit -> loadForEdit(mode.id, accounts, categories)
            }
        }
    }

    private fun loadForCreate(
        accounts: List<Account>,
        categories: List<Category>
    ) {
        val now = LocalDateTime.now()

        val initialTransaction = UiTransaction(
            id = null,
            accountId = accounts.firstOrNull()?.id!!,
            category = categories.firstOrNull()?.toUi()
                ?: UiCategory(0, "Unknown", "", false),
            amount = "0",
            date = DateHelper.formatDateForDisplay(LocalDate.now()),
            time = DateHelper.formatTimeForDisplay(LocalTime.now()),
            createdAt = DateHelper.formatDateTimeForDisplay(now),
            updatedAt = DateHelper.formatDateTimeForDisplay(now),
            currencyCode = accounts.firstOrNull()?.currency ?: CurrencySymbol.RUB.code,
            comment = "",
            mode = TransactionMode.Create
        )

        _uiState.update {
            TransactionUiState.Success(
                transaction = initialTransaction,
                categories = categories.map { it.toUi() },
                accounts = accounts.map { it.toUi() }
            )
        }
    }

    private suspend fun loadForEdit(
        id: Int,
        accounts: List<Account>,
        categories: List<Category>
    ) {
        getTransactionUseCase(id).fold(
            onSuccess = { transaction ->
                _uiState.update {
                    TransactionUiState.Success(
                        transaction = transaction.toUi(
                            category = categories.find { category -> category.id == transaction.categoryId }!!
                        ),
                        categories = categories.map { category -> category.toUi() },
                        accounts = accounts.map { account -> account.toUi() },
                    )
                }
            },
            onFailure = { _uiState.update { TransactionUiState.Error(R.string.error_load_transaction) } }
        )
    }

    fun save() {
        viewModelScope.launch {
            val state = uiState.value
            if (state !is TransactionUiState.Success) return@launch

            _uiState.update { TransactionUiState.Loading }

            val transaction =
                state.transaction.copy(updatedAt = DateHelper.formatDateTimeForDisplay(LocalDateTime.now()))
                    .toDomain()

            val result = if (mode == TransactionMode.Create) {
                createTransactionUseCase(transaction)
            } else {
                updateTransactionUseCase(transaction)
            }

            result.fold(
                onSuccess = { _eventFlow.emit(TransactionEvent.TransactionSaved) },
                onFailure = {
                    _eventFlow.emit(TransactionEvent.ShowError(R.string.error_save))
                    _uiState.update { state }
                }
            )
        }
    }

    fun updateComment(comment: String) {
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

    fun updateDate(date: LocalDate) {
        val currentState = uiState.value
        if (currentState is TransactionUiState.Success) {
            _uiState.update {
                currentState.copy(
                    transaction = currentState.transaction.copy(
                        date = DateHelper.formatDateForDisplay(date),
                        updatedAt = DateHelper.formatDateTimeForDisplay(LocalDateTime.now()),
                    )
                )
            }
        }

    }

    fun updateTime(time: LocalTime) {
        val currentState = uiState.value
        if (currentState is TransactionUiState.Success) {
            _uiState.update {
                currentState.copy(
                    transaction = currentState.transaction.copy(
                        time = DateHelper.formatTimeForDisplay(time),
                        updatedAt = DateHelper.formatDateTimeForDisplay(LocalDateTime.now()),
                    )
                )
            }
        }

    }

    fun updateAmount(amount: String) {
        val currentState = uiState.value
        if (currentState is TransactionUiState.Success) {
            _uiState.update {
                currentState.copy(
                    transaction = currentState.transaction.copy(
                        amount = amount
                    )
                )
            }
        }

    }

    fun updateCategory(category: UiCategory) {
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

    fun updateAccount(account: AccountUi) {
        val currentState = uiState.value
        if (currentState is TransactionUiState.Success) {
            _uiState.update {
                currentState.copy(
                    transaction = currentState.transaction.copy(
                        accountId = account.id,
                        currencyCode = account.currency
                    )
                )
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TransactionUiState.Success && mode is TransactionMode.Edit) {
                deleteTransactionUseCase(currentState.transaction.id!!).fold(
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
