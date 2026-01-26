package soft.divan.financemanager.feature.account.impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.feature.account.impl.R
import soft.divan.financemanager.feature.account.impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.impl.navigation.ACCOUNT_ID_KEY
import soft.divan.financemanager.feature.account.impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.account.impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountEvent
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountMode
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUi
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUiState
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val hapticsManager: HapticsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: String? = savedStateHandle.get<String>(ACCOUNT_ID_KEY)

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            AccountUiState.Loading
        )

    private val _eventFlow = MutableSharedFlow<AccountEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var account: AccountUi? = null

    private val mode =
        if (accountId == null) AccountMode.Create else AccountMode.Edit(accountId)

    private fun publishSuccess() {
        val currentAccount = account ?: return
        _uiState.update {
            AccountUiState.Success(
                account = currentAccount,
                mode = mode
            )
        }
    }

    fun loadAccount() {
        _uiState.update { AccountUiState.Loading }

        when (mode) {
            is AccountMode.Create -> initForCreate()
            is AccountMode.Edit -> loadAccount(mode.id)
        }
    }

    private fun initForCreate() {
        val now = LocalDateTime.now()
        account = AccountUi(
            id = UUID.randomUUID().toString(),
            name = "",
            balance = "0",
            currency = CurrencySymbol.RUB.symbol,
            createdAt = DateHelper.formatDateTimeForDisplay(now),
            updatedAt = DateHelper.formatDateTimeForDisplay(now),
        )

        publishSuccess()
    }

    private fun loadAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { AccountUiState.Loading }
            getAccountByIdUseCase(accountId).fold(
                onSuccess = {
                    account = it.toUi()
                    publishSuccess()
                },
                onFailure = { _uiState.update { AccountUiState.Error(R.string.error_save) } }
            )
        }
    }


    fun updateName(name: String) {
        account = account?.copy(name = name)
        publishSuccess()
    }

    fun updateBalance(balance: String) {
        account = account?.copy(balance = balance)
        publishSuccess()
    }

    fun updateCurrency(currency: String) {
        account = account?.copy(currency = currency)
        publishSuccess()
    }

    fun createAccount() {
        viewModelScope.launch {
            val current = account ?: return@launch

            _uiState.update { AccountUiState.Loading }

            val updated = current.copy(
                updatedAt = DateHelper.formatDateTimeForDisplay(LocalDateTime.now())
            )

            val result = when (mode) {
                is AccountMode.Create -> createAccountUseCase(updated.toDomain())
                is AccountMode.Edit -> updateAccountUseCase(updated.toDomain())
            }

            result.fold(
                onSuccess = {
                    hapticsManager.perform(HapticType.SUCCESS)
                    _eventFlow.emit(AccountEvent.Saved)
                },
                onFailure = {
                    hapticsManager.perform(HapticType.ERROR)
                    _eventFlow.emit(AccountEvent.ShowError(R.string.error_save))
                    publishSuccess()
                }
            )
        }
    }

    fun delete() {
        viewModelScope.launch {
            val id = account?.id ?: return@launch
            if (mode !is AccountMode.Edit) return@launch

            deleteAccountUseCase(id).fold(
                onSuccess = {
                    hapticsManager.perform(HapticType.SUCCESS)
                    _eventFlow.emit(AccountEvent.Deleted)
                },
                onFailure = {
                    hapticsManager.perform(HapticType.ERROR)
                    _eventFlow.emit(AccountEvent.ShowError(R.string.error_delete))
                    publishSuccess()
                }
            )
        }
    }
}