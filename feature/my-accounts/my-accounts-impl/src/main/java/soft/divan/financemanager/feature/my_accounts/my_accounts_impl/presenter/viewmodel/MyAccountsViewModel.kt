package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.viewmodel

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
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.R
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.mapper.toUiModel
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.MyAccountsUiState
import javax.inject.Inject

@HiltViewModel
class MyAccountsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyAccountsUiState>(MyAccountsUiState.Loading)
    val uiState: StateFlow<MyAccountsUiState> = _uiState
        .onStart { loadAccount() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            MyAccountsUiState.Loading
        )

    private suspend fun loadAccount() {
        getAccountsUseCase()
            .onStart {
                _uiState.update { MyAccountsUiState.Loading }
            }
            .onEach { data ->
                _uiState.update { MyAccountsUiState.Success(data.map { it.toUiModel() }) }
            }
            .catch {
                _uiState.update { MyAccountsUiState.Error(R.string.error_loading) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

}