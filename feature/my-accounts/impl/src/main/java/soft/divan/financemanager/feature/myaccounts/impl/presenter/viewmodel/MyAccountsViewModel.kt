package soft.divan.financemanager.feature.myaccounts.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.my_accounts.impl.R
import soft.divan.financemanager.feature.myaccounts.impl.presenter.mapper.toUiModel
import soft.divan.financemanager.feature.myaccounts.impl.presenter.model.MyAccountsUiState
import javax.inject.Inject

@HiltViewModel
class MyAccountsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val hapticsManager: HapticsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyAccountsUiState>(MyAccountsUiState.Loading)
    val uiState: StateFlow<MyAccountsUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            MyAccountsUiState.Loading
        )

    fun loadAccount() {
        getAccountsUseCase()
            .onStart { _uiState.update { MyAccountsUiState.Loading } }
            .onEach { result ->
                result.fold(
                    onSuccess = { accounts ->
                        val accounts = accounts.map { it.toUiModel() }
                        if (accounts.isEmpty()) {
                            _uiState.update { MyAccountsUiState.EmptyData }
                        } else {
                            _uiState.update { MyAccountsUiState.Success(accounts = accounts) }
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            MyAccountsUiState.Error(R.string.error_loading)
                        }
                    }
                )
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun hapticNavigation() {
        hapticsManager.perform(HapticType.CLICK)
    }
}
// Revue me>>
