package soft.divan.financemanager.feature.auth.impl.presenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.domain.repository.AuthRepository
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthAction
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthEvent
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUiState
import soft.divan.financemanager.sync.worker.SyncCoordinator
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val syncCoordinator: SyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Success())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val authStatus = getAuthStatusUseCase()

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.UpdateName -> {
                _uiState.updateContent { it.copy(authUi = it.authUi.copy(name = action.name)) }
            }

            is AuthAction.UpdatePassword -> {
                _uiState.updateContent { it.copy(authUi = it.authUi.copy(pass = action.pass)) }
            }

            AuthAction.ToggleMode -> {
                _uiState.updateContent {
                    it.copy(
                        authUi = it.authUi.copy(isLoginMode = !it.authUi.isLoginMode)
                    )
                }
            }

            AuthAction.OnAuthClick -> performAuth()

            is AuthAction.OnMergeConfirm -> onMergeConfirm(action.shouldMerge)

            AuthAction.OnLogoutClick -> {
                _uiState.updateContent { it.copy(showLogoutDialog = true) }
            }

            is AuthAction.OnLogoutConfirm -> logout(action.shouldClear)

            AuthAction.OnGuestClick -> signInAsGuest()

            AuthAction.DismissDialogs -> {
                _uiState.updateContent { it.copy(showMergeDialog = false, showLogoutDialog = false) }
            }
        }
    }

    private fun performAuth() {
        val content = _uiState.value as? AuthUiState.Success ?: return
        val name = content.authUi.name
        val pass = content.authUi.pass
        val isLogin = content.authUi.isLoginMode

        val previousContent = content
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val wasGuest = getAuthStatusUseCase().first() == AuthStatus.GUEST

            val result = if (isLogin) {
                authRepository.login(name, pass, shouldMergeData = true)
            } else {
                authRepository.register(name, pass, shouldMergeData = true)
            }

            result.fold(
                onSuccess = {
                    if (wasGuest) {
                        _uiState.value = previousContent.copy(showMergeDialog = true)
                    } else {
                        performInitialSync(previousContent)
                    }
                },
                onFailure = {
                    _uiState.value = previousContent.copy(
                        errorMessage = soft.divan.financemanager.feature.auth.impl.R.string.auth_error_failed
                    )
                }
            )
        }
    }

    private fun performInitialSync(successState: AuthUiState.Success) {
        _uiState.value = successState.copy(isSyncing = true)
        viewModelScope.launch {
            val isSyncSuccess = syncCoordinator.syncAll()
            if (!isSyncSuccess) {
                _eventFlow.emit(AuthEvent.ShowToast("Sync failed. Using local data."))
            }
            _eventFlow.emit(AuthEvent.NavigateToMain)
            _uiState.value = successState.copy(isSyncing = false)
        }
    }

    private fun onMergeConfirm(shouldMerge: Boolean) {
        val content = _uiState.value as? AuthUiState.Success ?: return
        _uiState.value = content.copy(showMergeDialog = false)
        viewModelScope.launch {
            if (!shouldMerge) {
                authRepository.clearUserData()
            }
            performInitialSync(content)
        }
    }

    private fun logout(shouldClear: Boolean) {
        val content = _uiState.value as? AuthUiState.Success ?: return
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.logout(shouldClear).fold(
                onSuccess = { _uiState.value = AuthUiState.Success() },
                onFailure = { _uiState.value = content }
            )
        }
    }

    private fun signInAsGuest() {
        viewModelScope.launch {
            authRepository.loginAsGuest().fold(
                onSuccess = { _eventFlow.emit(AuthEvent.NavigateToMain) },
                onFailure = { /* ignore */ }
            )
        }
    }

    private fun MutableStateFlow<AuthUiState>.updateContent(update: (AuthUiState.Success) -> AuthUiState.Success) {
        this.update { currentState ->
            if (currentState is AuthUiState.Success) update(currentState) else currentState
        }
    }
}
