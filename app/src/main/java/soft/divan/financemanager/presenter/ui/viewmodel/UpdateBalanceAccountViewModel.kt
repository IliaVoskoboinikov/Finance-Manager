package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import soft.divan.financemanager.presenter.ui.model.UpdateBalanceAccountUiState
import javax.inject.Inject


@HiltViewModel
class UpdateBalanceAccountViewModel @Inject constructor(
    private val updateAccountUseCase: UpdateAccountUseCase,
) : ViewModel() {
    private val _uiState = MutableSharedFlow<UpdateBalanceAccountUiState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiState: SharedFlow<UpdateBalanceAccountUiState> = _uiState.asSharedFlow()
    private val _balance = MutableStateFlow("10")
    val balance: StateFlow<String> = _balance.asStateFlow()

    public fun updateBalance(accountBrief: AccountBrief) {
        updateAccountUseCase(accountBrief.copy(balance = balance.value.toBigDecimal()))
            .onStart {
                _uiState.tryEmit(UpdateBalanceAccountUiState.Loading)
            }
            .onEach {
                _uiState.tryEmit(UpdateBalanceAccountUiState.Success(Unit))
            }
            .catch { exception ->
                _uiState.tryEmit(UpdateBalanceAccountUiState.Error(exception.message.toString()))
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)


    }

    fun onBalanceChanged(newValue: String) {
        _balance.value = newValue
    }


}
