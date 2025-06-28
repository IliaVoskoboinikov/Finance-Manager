package soft.divan.financemanager.presenter.ui.viewmodel

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
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.presenter.mapper.toUiModel
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import javax.inject.Inject


@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
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


}
