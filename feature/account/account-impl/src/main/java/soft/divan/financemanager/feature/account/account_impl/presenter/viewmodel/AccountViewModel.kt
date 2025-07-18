package soft.divan.financemanager.feature.account.account_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateCurrencyUseCase
import soft.divan.financemanager.feature.account.account_impl.presenter.mapper.toDomain
import soft.divan.financemanager.feature.account.account_impl.presenter.mapper.toUiModel
import soft.divan.financemanager.feature.account.account_impl.presenter.model.AccountUiState
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val updateCurrencyUseCase: UpdateCurrencyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000L),
            AccountUiState.Loading
        )

    private fun loadAccount() {
        getAccountsUseCase.invoke()
            .onStart {
                _uiState.update { AccountUiState.Loading }
            }
            .onEach { data ->
                _uiState.update { AccountUiState.Success(data.first().toUiModel()) }
            }
            .catch { exception ->
                _uiState.update { AccountUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun updateCurrency(currency: String) {
        val currentState = uiState.value

        if (currentState !is AccountUiState.Success) return

        viewModelScope.launch {
            updateAccountUseCase.invoke(currentState.account.copy(currency = currency).toDomain())
                .onStart {
                    _uiState.update { AccountUiState.Loading }
                }
                .onEach { data ->
                    _uiState.update { AccountUiState.Success(data.toUiModel()) }
                }
                .catch { exception ->
                    _uiState.update { AccountUiState.Error(exception.message.toString()) }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(viewModelScope)

            updateCurrencyUseCase(CurrencyCode(CurrencySymbol.fromSymbol(currency)))
        }
    }

    fun updateName(name: String) {
        val currentState = uiState.value

        if (currentState !is AccountUiState.Success) return

        updateAccountUseCase.invoke(currentState.account.copy(name = name).toDomain())
            .onStart {
                _uiState.update { AccountUiState.Loading }
            }
            .onEach { data ->
                _uiState.update { AccountUiState.Success(data.toUiModel()) }
            }
            .catch { exception ->
                _uiState.update { AccountUiState.Error(exception.message.toString()) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }


}