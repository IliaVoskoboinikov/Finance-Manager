package soft.divan.financemanager.feature.transaction.impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.sounds.api.domain.SoundPlayer
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.transaction.impl.R
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.transaction.impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.EmptyReason
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUiState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@HiltViewModel(assistedFactory = TransactionViewModel.Factory::class)
@Suppress("LongParameterList", "TooManyFunctions")
class TransactionViewModel @AssistedInject constructor(
    private val createTransactionAndUpdateAccountUseCase: CreateTransactionAndUpdateAccountUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesByTypeUseCase,
    private val updateTransactionAndUpdateAccountUseCase: UpdateTransactionAndUpdateAccountUseCase,
    private val deleteTransactionAndUpdateAccountUseCase: DeleteTransactionAndUpdateAccountUseCase,
    private val hapticsManager: HapticsManager,
    private val soundPlayer: SoundPlayer,
    @Assisted private val isIncome: Boolean,
    @Assisted private val transactionId: String?
) : ViewModel() {

    /** Создаёт [TransactionViewModel] с аргументами навигации из `TransactionKey`. */
    @AssistedFactory
    interface Factory {
        fun create(isIncome: Boolean, transactionId: String?): TransactionViewModel
    }

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Loading)
    val uiState: StateFlow<TransactionUiState> = _uiState.onStart { load() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            TransactionUiState.Loading
        )

    private val _eventFlow = MutableSharedFlow<TransactionEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var transactionUi: TransactionUi? = null
    private var categories: List<CategoryUi> = emptyList()
    private var accounts: List<AccountUi> = emptyList()

    /** Идущая загрузка: в пустом состоянии она продолжает слушать БД. */
    private var loadJob: Job? = null

    /** Идущая запись (сохранение или удаление): пока она не завершилась, новая не запускается. */
    private var writeJob: Job? = null

    /**
     * Запись прошла успешно, экран закрывается. Флаг не сбрасывается: тап, пришедший после
     * успеха, но до закрытия экрана, повторил бы операцию и ещё раз изменил баланс.
     */
    private var isWriteCompleted = false

    private val mode =
        if (transactionId == null) TransactionMode.Create else TransactionMode.Edit(transactionId)

    private val moneyRegex =
        Regex("^(0|[1-9]\\d*)(\\.\\d{0,2})?$")

    private fun publishSuccess() {
        val currentTransaction = transactionUi ?: return
        _uiState.update {
            TransactionUiState.Success(
                transaction = currentTransaction,
                categories = categories,
                accounts = accounts
            )
        }
    }

    /**
     * Загружает счета и категории и собирает форму. Для новой операции без счёта или категории
     * показывает [TransactionUiState.Empty] и продолжает слушать БД, пока данные не появятся.
     * Повторный вызов отменяет предыдущую загрузку.
     */
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { TransactionUiState.Loading }
            // takeWhile, а не first { }: тот бросает исключение, если поток кончится пустым
            combine(getCategoriesUseCase(isIncome), getAccountsUseCase(), ::Pair)
                .takeWhile { (categoriesResult, accountsResult) ->
                    !applyLoaded(categoriesResult, accountsResult)
                }
                .collect()
        }
    }

    /**
     * Применяет очередную выдачу счетов и категорий. Возвращает `true`, когда загрузка
     * закончена (форма собрана или показана ошибка), и `false`, если ждём данных дальше.
     */
    private suspend fun applyLoaded(
        categoriesResult: DomainResult<List<Category>>,
        accountsResult: DomainResult<List<Account>>
    ): Boolean {
        if (categoriesResult !is DomainResult.Success || accountsResult !is DomainResult.Success) {
            _uiState.update { TransactionUiState.Error(R.string.error_load_transaction) }
            return true
        }

        categories = categoriesResult.data.map { it.toUi() }
        accounts = accountsResult.data.map { it.toUi() }

        return when (mode) {
            is TransactionMode.Create -> applyLoadedForCreate()

            is TransactionMode.Edit -> {
                initForEdit(mode.id)
                true
            }
        }
    }

    /** Собирает новую операцию или, если не хватает данных, показывает пустое состояние. */
    private fun applyLoadedForCreate(): Boolean {
        val emptyReason = emptyReason()
        if (emptyReason != null) {
            _uiState.update { TransactionUiState.Empty(emptyReason) }
        } else {
            initForCreate()
        }
        return emptyReason == null
    }

    /** Счёт важнее категорий: его пользователь может создать сам, прямо с этого экрана. */
    private fun emptyReason(): EmptyReason? = when {
        accounts.isEmpty() -> EmptyReason.NO_ACCOUNTS
        categories.isEmpty() -> EmptyReason.NO_CATEGORIES
        else -> null
    }

    private fun initForCreate() {
        val now = LocalDateTime.now()
        val account = accounts.first()
        val category = categories.first()

        transactionUi = TransactionUi(
            id = generateUUID(),
            accountId = account.id,
            category = category,
            amount = "0",
            date = UiDateFormatter.formatDate(LocalDate.now()),
            time = UiDateFormatter.formatTime(LocalTime.now()),
            createdAt = UiDateFormatter.formatDateTime(now),
            updatedAt = UiDateFormatter.formatDateTime(now),
            currencyId = account.currencyId,
            comment = "",
            mode = TransactionMode.Create,
            targetAccountLocalId = null,
            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        )

        publishSuccess()
    }

    private suspend fun initForEdit(id: String) {
        getTransactionUseCase(id).fold(
            onSuccess = { domainTransaction ->
                val category = categories.firstOrNull { it.id == domainTransaction.categoryId }
                if (category == null) {
                    _uiState.update { TransactionUiState.Error(R.string.error_load_transaction) }
                    return@fold
                }

                transactionUi = domainTransaction.toUi(category)
                resolveArchivedAccountIfNeeded()
                publishSuccess()
            },
            onFailure = {
                _uiState.update {
                    TransactionUiState.Error(R.string.error_load_transaction)
                }
            }
        )
    }

    /**
     * Если операция ссылается на счёт, которого нет в списке для выбора (он архивный и отфильтрован
     * [GetAccountsUseCase]), подтягивает его по id и добавляет как архивный — чтобы экран показал
     * его имя. В пикере такой счёт по-прежнему недоступен для выбора.
     */
    private suspend fun resolveArchivedAccountIfNeeded() {
        val accountId = transactionUi?.accountId ?: return
        if (accounts.any { it.id == accountId }) return

        getAccountByIdUseCase(accountId).onSuccess { account ->
            accounts = accounts + account.toUi()
        }
    }

    /**
     * Запускает запись [block], если другой записи сейчас нет и экран ещё не закрывается.
     * Вызывается с главного потока, поэтому проверка и запуск не гоняются между собой.
     */
    private fun launchWrite(block: suspend CoroutineScope.() -> Unit) {
        if (isWriteCompleted || writeJob?.isActive == true) return
        writeJob = viewModelScope.launch(block = block)
    }

    fun save() {
        launchWrite {
            val current = transactionUi ?: return@launchWrite

            _uiState.update { TransactionUiState.Loading }

            val updated = current.copy(
                updatedAt = UiDateFormatter.formatDateTime(LocalDateTime.now())
            ).toDomain()

            val result = when (mode) {
                is TransactionMode.Create -> createTransactionAndUpdateAccountUseCase(updated)
                is TransactionMode.Edit -> updateTransactionAndUpdateAccountUseCase(updated)
            }

            result.fold(
                onSuccess = {
                    isWriteCompleted = true
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
        transactionUi = transactionUi?.copy(comment = comment)
        publishSuccess()
    }

    fun updateDate(date: LocalDate) {
        transactionUi =
            transactionUi?.copy(date = UiDateFormatter.formatDate(date))
        publishSuccess()
    }

    fun updateTime(time: LocalTime) {
        transactionUi = transactionUi?.copy(time = UiDateFormatter.formatTime(time))
        publishSuccess()
    }

    fun onAmountInputChanged(input: String) {
        val current = transactionUi?.amount ?: "0"

        val newAmount = when {
            input.isEmpty() -> "0"
            current == "0" && input.length == 2 && input[1].isDigit() -> input.last().toString()
            moneyRegex.matches(input) -> input
            else -> return
        }

        transactionUi = transactionUi?.copy(amount = newAmount)
        publishSuccess()
    }

    fun updateCategory(category: CategoryUi) {
        transactionUi = transactionUi?.copy(category = category)
        publishSuccess()
    }

    fun updateAccount(account: AccountUi) {
        transactionUi = transactionUi?.copy(
            accountId = account.id,
            currencyId = account.currencyId
        )
        publishSuccess()
    }

    fun delete() {
        launchWrite {
            val transaction = transactionUi ?: return@launchWrite
            if (mode !is TransactionMode.Edit) return@launchWrite

            deleteTransactionAndUpdateAccountUseCase(transaction.toDomain()).fold(
                onSuccess = {
                    isWriteCompleted = true
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
