package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class AccountItem {
    data class Balance(
        val emoji: String,
        val label: String,
        val amount: String
    ) : AccountItem()

    data class Currency(
        val label: String,
        val symbol: String
    ) : AccountItem()
}

sealed class AccountUiState {
    data object Loading : AccountUiState()
    data class Success(val items: List<AccountItem>) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}

class AccountViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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
            _uiState.value = AccountUiState.Success(mockAccount)
        }
    }
}
