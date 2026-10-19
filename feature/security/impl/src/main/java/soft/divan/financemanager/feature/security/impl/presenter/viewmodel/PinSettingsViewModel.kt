package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.feature.security.impl.domain.model.DeletePinResult
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityFlowState
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import javax.inject.Inject

/**
 * Смена и удаление PIN на экране настроек. Обе операции требуют текущий PIN: без него на уровне
 * PIN не достать ключ данных, а на остальных — нельзя позволить снять замок чужому.
 */
@HiltViewModel
class PinSettingsViewModel @Inject constructor(
    private val changePin: ChangePinUseCase,
    private val deletePin: DeletePinUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityFlowState())
    val state: StateFlow<SecurityFlowState> = _state.asStateFlow()

    /** «Сменить PIN»: сначала текущий PIN. */
    fun onChangePinClicked() {
        _state.update { it.copy(pinPrompt = PinPrompt(PinPurpose.ChangePinCurrent)) }
    }

    /** «Удалить PIN»; на уровне PIN запрещено — им завёрнут ключ данных. */
    fun onDeletePinClicked(dataProtectedByPin: Boolean) {
        _state.update {
            if (dataProtectedByPin) {
                it.copy(message = SecurityMessage.DELETE_NOT_ALLOWED)
            } else {
                it.copy(pinPrompt = PinPrompt(PinPurpose.DeletePin))
            }
        }
    }

    /** Введён PIN для текущего шага. */
    fun onPinEntered(pin: String) {
        val prompt = _state.value.pinPrompt ?: return
        _state.update { it.copy(pinPrompt = null) }
        when (val purpose = prompt.purpose) {
            PinPurpose.ChangePinCurrent ->
                _state.update {
                    it.copy(
                        pinPrompt = PinPrompt(PinPurpose.ChangePinNew(pin), create = true)
                    )
                }

            is PinPurpose.ChangePinNew -> runBusy {
                when (changePin(purpose.currentPin, pin)) {
                    ProtectionChangeResult.Changed -> SecurityMessage.PIN_CHANGED

                    ProtectionChangeResult.WrongPin -> SecurityMessage.WRONG_PIN

                    ProtectionChangeResult.UnsentChanges, ProtectionChangeResult.Failed ->
                        SecurityMessage.CHANGE_FAILED
                }
            }

            PinPurpose.DeletePin -> runBusy {
                when (deletePin(pin)) {
                    DeletePinResult.DELETED -> SecurityMessage.PIN_DELETED
                    DeletePinResult.WRONG_PIN -> SecurityMessage.WRONG_PIN
                    DeletePinResult.NOT_ALLOWED -> SecurityMessage.DELETE_NOT_ALLOWED
                }
            }

            else -> Unit
        }
    }

    /** Ввод PIN закрыт. */
    fun onDismissed() {
        _state.update { it.copy(pinPrompt = null) }
    }

    /** Сообщение показано. */
    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun runBusy(block: suspend () -> SecurityMessage) {
        _state.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            val message = block()
            _state.update { it.copy(inProgress = false, message = message) }
        }
    }
}
