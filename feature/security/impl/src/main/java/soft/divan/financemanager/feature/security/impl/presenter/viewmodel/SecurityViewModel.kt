package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState
import javax.inject.Inject

@HiltViewModel
open class SecurityViewModel @Inject constructor(
    private val getSavedPinUseCase: GetSavedPinUseCase,
    private val isPinSetUseCase: IsPinSetUseCase,
    private val deletePinUseCase: DeletePinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SecurityUiState>(SecurityUiState.Loading)
    val uiState: StateFlow<SecurityUiState> = _uiState
        .onStart { loadPin() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            SecurityUiState.Loading
        )

    fun loadPin() {
        val savedPin = getSavedPinUseCase() ?: ""
        _uiState.update {
            SecurityUiState.Success(pin = savedPin, hasPin = isPinSetUseCase())
        }
    }

    fun deletePin() {
        val currentState = uiState.value
        if (currentState is SecurityUiState.Success) {
            deletePinUseCase()
            _uiState.update { SecurityUiState.Success(pin = "", hasPin = false) }
        }
    }
}
// Revue me>>
