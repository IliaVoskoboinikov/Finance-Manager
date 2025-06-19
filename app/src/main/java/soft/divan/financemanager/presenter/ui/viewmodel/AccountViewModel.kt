package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.domain.utils.Rezult
import soft.divan.financemanager.presenter.mapper.AccountUiStateMapper
import soft.divan.financemanager.presenter.ui.model.AccountItem
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import javax.inject.Inject


@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val uiStateMapper: AccountUiStateMapper,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val mockAccount = listOf(
        AccountItem.Balance("💰", "Все счета", "-670 000 ₽"),
        AccountItem.Currency("Валюта", "₽")
    )

    init {
        loadAccount()
    }

    private fun loadAccount() {
        viewModelScope.launch(dispatcher) {
            when (val result = getAccountsUseCase()) {
                is Rezult.Error -> {
                    _uiState.update { AccountUiState.Error(result.exception.message.toString()) }
                }

                is Rezult.Success -> {
                    _uiState.update { uiStateMapper.mapToUiState(result.data) }
                }
            }
        }
    }


}
