package soft.divan.financemanager.feature.transaction.impl.precenter.viewModel

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
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.api.domain.SoundsPlayer
import soft.divan.financemanager.feature.transaction.impl.R
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.transaction.impl.navigation.TRANSACTION_ID_KEY
import soft.divan.financemanager.feature.transaction.impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.transaction.impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUiState
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
    private val hapticsManager: HapticsManager,
    private val soundPlayer: SoundsPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: String? = savedStateHandle.get<String>(TRANSACTION_ID_KEY)
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

    private var transaction: TransactionUi? = null
    private var categories: List<CategoryUi> = emptyList()
    private var accounts: List<AccountUi> = emptyList()

    private val mode =
        if (transactionId == null) TransactionMode.Create else TransactionMode.Edit(transactionId)

    private fun publishSuccess() {
        val currentTransaction = transaction ?: return
        _uiState.update {
            TransactionUiState.Success(
                transaction = currentTransaction,
                categories = categories,
                accounts = accounts
            )
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { TransactionUiState.Loading }
            val categoriesResult = getCategoriesUseCase(isIncome).first()
            val accountsResult = getAccountsUseCase().first()

            if (categoriesResult !is DomainResult.Success || accountsResult !is DomainResult.Success) {
                _uiState.update { TransactionUiState.Error(R.string.error_load_transaction) }
                return@launch
            }

            categories = categoriesResult.data.map { it.toUi() }
            accounts = accountsResult.data.map { it.toUi() }

            when (mode) {
                is TransactionMode.Create -> initForCreate()
                is TransactionMode.Edit -> initForEdit(mode.id)
            }
        }
    }

    private fun initForCreate() {
        val now = LocalDateTime.now()
        val account = accounts.firstOrNull() ?: return
        val category = categories.firstOrNull() ?: return

        transaction = TransactionUi(
            id = generateUUID(),
            accountId = account.id,
            category = category,
            amount = "0",
            date = DateHelper.formatDateForDisplay(LocalDate.now()),
            time = DateHelper.formatTimeForDisplay(LocalTime.now()),
            createdAt = DateHelper.formatDateTimeForDisplay(now),
            updatedAt = DateHelper.formatDateTimeForDisplay(now),
            currencyCode = account.currency,
            comment = "",
            mode = TransactionMode.Create
        )

        publishSuccess()
    }

    private suspend fun initForEdit(id: String) {
        getTransactionUseCase(id).fold(
            onSuccess = { domainTransaction ->
                val category = categories.firstOrNull { it.id == domainTransaction.categoryId }
                    ?: return@fold

                transaction = domainTransaction.toUi(category)
                publishSuccess()
            },
            onFailure = { _uiState.update { TransactionUiState.Error(R.string.error_load_transaction) } }
        )
    }

    fun save() {
        viewModelScope.launch {
            val current = transaction ?: return@launch

            _uiState.update { TransactionUiState.Loading }

            val updated = current.copy(
                updatedAt = DateHelper.formatDateTimeForDisplay(LocalDateTime.now())
            ).toDomain()

            val result = when (mode) {
                is TransactionMode.Create -> createTransactionUseCase(updated)
                is TransactionMode.Edit -> updateTransactionUseCase(updated)
            }

            result.fold(
                onSuccess = {
                    hapticsManager.perform(HapticType.SUCCESS)
                    soundPlayer.play(SoundType.COIN)
                    _eventFlow.emit(TransactionEvent.TransactionSaved)
                },
                onFailure = {
                    hapticsManager.perform(HapticType.ERROR)
                    _eventFlow.emit(TransactionEvent.ShowError(R.string.error_save))
                    publishSuccess()
                }
            )
        }
    }

    fun updateComment(comment: String) {
        transaction = transaction?.copy(comment = comment)
        publishSuccess()
    }

    fun updateDate(date: LocalDate) {
        transaction = transaction?.copy(date = DateHelper.formatDateForDisplay(date))
        publishSuccess()
    }

    fun updateTime(time: LocalTime) {
        transaction = transaction?.copy(time = DateHelper.formatTimeForDisplay(time))
        publishSuccess()
    }

    fun updateAmount(amount: String) {
        transaction = transaction?.copy(amount = amount)
        publishSuccess()
    }

    fun updateCategory(category: CategoryUi) {
        transaction = transaction?.copy(category = category)
        publishSuccess()
    }

    fun updateAccount(account: AccountUi) {
        transaction = transaction?.copy(
            accountId = account.id,
            currencyCode = account.currency
        )
        publishSuccess()
    }

    fun delete() {
        viewModelScope.launch {
            val id = transaction?.id ?: return@launch
            if (mode !is TransactionMode.Edit) return@launch

            deleteTransactionUseCase(id).fold(
                onSuccess = {
                    hapticsManager.perform(HapticType.SUCCESS)
                    _eventFlow.emit(TransactionEvent.TransactionDeleted)
                },
                onFailure = {
                    hapticsManager.perform(HapticType.ERROR)
                    _eventFlow.emit(TransactionEvent.ShowError(R.string.fail_delete_transaction))
                    publishSuccess()
                }
            )
        }
    }
}
