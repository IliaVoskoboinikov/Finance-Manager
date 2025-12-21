package soft.divan.financemanager.feature.account.account_impl.precenter.viewModel

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
import soft.divan.financemanager.feature.account.account_impl.R
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.navigation.ACCOUNT_ID_KEY
import soft.divan.financemanager.feature.account.account_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.account.account_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountEvent
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountMode
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountUiModel
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountUiState
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
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

    private val mode =
        if (accountId == null) AccountMode.Create else AccountMode.Edit(accountId)

    fun loadAccount() {
        _uiState.update { AccountUiState.Loading }

        when (mode) {
            is AccountMode.Create -> crateNewAccount()
            is AccountMode.Edit -> loadAccount(mode.id)
        }
    }

    private fun crateNewAccount() {
        val now = LocalDateTime.now()
        val account = AccountUiModel(
            UUID.randomUUID().toString(), "", "", CurrencySymbol.RUB.symbol,
            createdAt = DateHelper.formatDateTimeForDisplay(now),
            updatedAt = DateHelper.formatDateTimeForDisplay(now),
        )
        _uiState.update {
            AccountUiState.Success(
                account = account,
                mode = mode
            )
        }
    }

    private fun loadAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { AccountUiState.Loading }
            getAccountByIdUseCase(accountId).fold(
                onSuccess = { data ->
                    _uiState.update {
                        AccountUiState.Success(
                            account = data.toUi(),
                            mode = mode
                        )
                    }
                },
                onFailure = { _uiState.update { AccountUiState.Error(R.string.error_save) } }
            )
        }
    }


    fun updateName(name: String) {
        val currentState = uiState.value
        if (currentState is AccountUiState.Success) {
            _uiState.update { currentState.copy(account = currentState.account.copy(name = name)) }
        }
    }

    fun updateBalance(balance: String) {
        val currentState = uiState.value
        if (currentState is AccountUiState.Success) {
            _uiState.update { currentState.copy(account = currentState.account.copy(balance = balance)) }
        }
    }

    fun updateCurrency(currency: String) {
        val currentState = uiState.value
        if (currentState is AccountUiState.Success) {
            _uiState.update { currentState.copy(account = currentState.account.copy(currency = currency)) }
        }
    }


    fun createAccount() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
                _uiState.update { AccountUiState.Loading }

                val account = currentState.account.toDomain()

                val result = when (currentState.mode) {
                    is AccountMode.Create -> createAccountUseCase(account)
                    is AccountMode.Edit -> updateAccountUseCase(account)
                }

                result.fold(
                    onSuccess = { _eventFlow.emit(AccountEvent.Saved) },
                    onFailure = {
                        _eventFlow.emit(AccountEvent.ShowError(R.string.error_save))
                        _uiState.update { currentState }
                    }
                )
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success && mode is AccountMode.Edit) {
                deleteAccountUseCase(currentState.account.id).fold(
                    onSuccess = { _eventFlow.emit(AccountEvent.Deleted) },
                    onFailure = {
                        _eventFlow.emit(AccountEvent.ShowError(R.string.error_delete))
                        _uiState.update { currentState }
                    }
                )
            }
        }
    }
}