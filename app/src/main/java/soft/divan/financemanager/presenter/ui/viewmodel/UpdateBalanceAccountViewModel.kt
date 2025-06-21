package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import soft.divan.financemanager.domain.utils.Rezult
import soft.divan.financemanager.presenter.ui.model.UpdateBalanceAccountUiState
import javax.inject.Inject


@HiltViewModel
class UpdateBalanceAccountViewModel @Inject constructor(
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableSharedFlow<UpdateBalanceAccountUiState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val uiState: SharedFlow<UpdateBalanceAccountUiState> = _uiState.asSharedFlow()
    private val _balance = MutableStateFlow("")
    val balance: StateFlow<String> = _balance.asStateFlow()

    public fun updateBalance(accountBrief: AccountBrief) {
        viewModelScope.launch(dispatcher) {
            when (val result = updateAccountUseCase(accountBrief.copy(balance = balance.value.toBigDecimal()))) {
                is Rezult.Error -> {
                    _uiState.tryEmit( UpdateBalanceAccountUiState.Error(result.exception.message.toString()) )
                }

                is Rezult.Success -> {
                    _uiState.tryEmit(UpdateBalanceAccountUiState.Success(Unit) )
                }
            }
        }
    }

    fun onBalanceChanged(newValue: String) {
        _balance.value = newValue
    }




}
