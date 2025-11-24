package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountEvent
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model.CreateAccountUiState
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateAccountUiState>(CreateAccountUiState.Loading)
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateAccountEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun createAccount() {

    }
}