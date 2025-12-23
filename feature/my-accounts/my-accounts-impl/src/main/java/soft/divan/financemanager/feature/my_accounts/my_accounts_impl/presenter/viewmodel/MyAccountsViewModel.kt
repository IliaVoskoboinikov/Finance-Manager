package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.haptic.haptic_api.domain.HapticManager
import soft.divan.financemanager.feature.haptic.haptic_api.domain.HapticType
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.R
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.mapper.toUiModel
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.MyAccountsUiState
import javax.inject.Inject

@HiltViewModel
class MyAccountsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val hapticManager: HapticManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyAccountsUiState>(MyAccountsUiState.Loading)
    val uiState: StateFlow<MyAccountsUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            MyAccountsUiState.Loading
        )

    fun loadAccount() {
        viewModelScope.launch {
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
                        onFailure = { _uiState.update { MyAccountsUiState.Error(R.string.error_loading) } }
                    )
                }.collect()
        }
    }

    fun hapticNavigation() {
        hapticManager.perform(HapticType.CLICK)
    }

}