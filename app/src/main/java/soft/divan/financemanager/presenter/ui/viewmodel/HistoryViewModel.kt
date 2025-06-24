package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    /*private val getAccountsUseCase: GetAccountsUseCase,
    private val uiStateMapper: AccountUiStateMapper,*/

) : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            /*when (val result = getAccountsUseCase()) {
                is Rezult.Error -> {
                    _uiState.update { AccountUiState.Error(result.exception.message.toString()) }
                }

                is Rezult.Success -> {
                    _uiState.update { uiStateMapper.mapToUiState(result.data) }
                }
            }*/
        }
    }
}