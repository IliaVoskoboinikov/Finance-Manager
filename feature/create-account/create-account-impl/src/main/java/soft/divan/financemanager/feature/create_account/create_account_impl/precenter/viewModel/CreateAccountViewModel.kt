package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.create_account.create_account_impl.R
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.AccountUiModel
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountEvent
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountUiState
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateAccountUiState>(
        CreateAccountUiState.Success(
            account = AccountUiModel(
                name = "",
                balance = "",
                currency = CurrencySymbol.RUB.symbol
            )
        )
    )
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateAccountEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun updateName(name: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CreateAccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(name = name)) }
            }
        }
    }

    fun updateBalance(balance: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CreateAccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(balance = balance)) }
            }
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CreateAccountUiState.Success) {
                _uiState.update { currentState.copy(account = currentState.account.copy(currency = currency)) }
            }
        }
    }


    fun createAccount() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CreateAccountUiState.Success) {
                _uiState.update { CreateAccountUiState.Loading }
                createAccountUseCase(currentState.account.toDomain())
                    .fold(
                        onSuccess = { _eventFlow.emit(CreateAccountEvent.Saved) },
                        onFailure = {
                            _eventFlow.emit(CreateAccountEvent.ShowError(R.string.error_save))
                            _uiState.update { currentState }
                        }
                    )
            }
        }
    }
}