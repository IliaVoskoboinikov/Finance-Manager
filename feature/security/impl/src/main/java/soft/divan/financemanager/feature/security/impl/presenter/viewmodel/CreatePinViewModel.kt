package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinError
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinStep
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinUiState
import javax.inject.Inject

/**
 * Создание PIN: придумать → повторить → сохранить.
 *
 * Сохранение (PBKDF2 + KeyStore) идёт в корутине на IO, а не из композиции. Оно не идемпотентно
 * (каждый раз новая соль), поэтому ввод закрыт, пока PIN сохраняется, и после сохранения —
 * пока экран закрывается.
 */
@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val savePinUseCase: SavePinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePinUiState())
    val uiState: StateFlow<CreatePinUiState> = _uiState.asStateFlow()

    /** Первый ввод ждёт повтора; в UiState не публикуется. */
    private var firstPin: String? = null

    /** Введён PIN на текущем шаге. */
    fun onPinEntered(pin: String) {
        if (!_uiState.value.isInputEnabled) return
        val first = firstPin
        when {
            first == null -> {
                firstPin = pin
                _uiState.update { it.copy(step = CreatePinStep.CONFIRM, error = null) }
            }

            first != pin -> startOver(CreatePinError.MISMATCH)

            else -> save(pin)
        }
    }

    private fun save(pin: String) {
        // Флаг ставится до первой приостановки: повторный ввод его уже видит
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            savePinUseCase(pin).fold(
                onSuccess = {
                    firstPin = null
                    _uiState.update { it.copy(isSaving = false, isCompleted = true) }
                },
                onFailure = { startOver(CreatePinError.SAVE_FAILED) }
            )
        }
    }

    private fun startOver(error: CreatePinError) {
        firstPin = null
        _uiState.update { CreatePinUiState(error = error) }
    }
}
