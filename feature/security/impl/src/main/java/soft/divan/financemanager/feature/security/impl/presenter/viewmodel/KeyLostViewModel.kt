package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.RecoverLocalDataUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.KeyLostUiState
import javax.inject.Inject

/**
 * Экран «ключ данных утерян». Выход отсюда — смена состояния данных: хост сам уберёт экран,
 * когда данные откроются.
 */
@HiltViewModel
class KeyLostViewModel @Inject constructor(
    private val recoverLocalData: RecoverLocalDataUseCase,
    private val getAuthStatus: GetAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeyLostUiState())
    val uiState: StateFlow<KeyLostUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val guest = getAuthStatus().first() != AuthStatus.AUTHORIZED
            _uiState.update { it.copy(isGuest = guest) }
        }
    }

    /** Ещё раз прочитать ключи — вдруг сбой был временным. */
    fun onRetry() = runBusy { recoverLocalData.retry() }

    /** Показать предупреждение перед стиранием. */
    fun onWipeClicked() {
        _uiState.update { it.copy(showConfirm = true) }
    }

    /** Предупреждение закрыто без стирания. */
    fun onWipeDismissed() {
        _uiState.update { it.copy(showConfirm = false) }
    }

    /** Стереть недоступные данные и начать заново. */
    fun onWipeConfirmed() {
        _uiState.update { it.copy(showConfirm = false) }
        runBusy { recoverLocalData.recover() }
    }

    private fun runBusy(block: suspend () -> Unit) {
        if (_uiState.value.inProgress) return
        _uiState.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                _uiState.update { it.copy(inProgress = false) }
            }
        }
    }
}
