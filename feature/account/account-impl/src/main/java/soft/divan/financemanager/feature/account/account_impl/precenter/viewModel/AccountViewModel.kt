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
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.account.account_impl.R
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.navigation.accountIdKey
import soft.divan.financemanager.feature.account.account_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.account.account_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountEvent
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountUiModel
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountUiState
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    savedStateHandle: SavedStateHandle

) : ViewModel() {
    private val accountId: Int? = savedStateHandle.get<Int>(accountIdKey)

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

    fun loadAccount() {
        _uiState.update { AccountUiState.Loading }
        if (accountId == null || accountId == -1) {
            crateNewAccount()
        } else {
            loadOldAccount(accountId)
        }
    }

    private fun crateNewAccount() {
        _uiState.update {
            AccountUiState.Success(
                account = AccountUiModel(
                    id = -1,
                    name = "",
                    balance = "",
                    currency = CurrencySymbol.RUB.symbol
                )
            )
        }
    }

    private fun loadOldAccount(accountId: Int) {
        viewModelScope.launch {
            getAccountByIdUseCase(accountId)
                .fold(
                    onSuccess = { account -> _uiState.update { AccountUiState.Success(account.toUi()) } },
                    onFailure = { _uiState.update { AccountUiState.Error(R.string.error_save) } }
                )
        }
    }


    fun updateName(name: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(name = name)) }
            }
        }
    }

    fun updateBalance(balance: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(balance = balance)) }
            }
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(currency = currency)) }
            }
        }
    }


    fun createAccount() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
                _uiState.update { AccountUiState.Loading }
                if (currentState.account.id != -1) {
                    updateAccountUseCase(
                        account = currentState.account.toDomain()
                    )
                        .fold(
                            onSuccess = { _eventFlow.emit(AccountEvent.Saved) },
                            onFailure = {
                                _eventFlow.emit(AccountEvent.ShowError(R.string.error_save))
                                _uiState.update { currentState }
                            }
                        )
                } else {
                    createAccountUseCase(currentState.account.toDomain())
                        .fold(
                            onSuccess = { _eventFlow.emit(AccountEvent.Saved) },
                            onFailure = {
                                _eventFlow.emit(AccountEvent.ShowError(R.string.error_save))
                                _uiState.update { currentState }
                            }
                        )
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is AccountUiState.Success) {
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