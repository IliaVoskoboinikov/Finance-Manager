package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.vault.BiometricEnrollmentStart
import soft.divan.financemanager.core.data.vault.BiometricSession
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityFlowState
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import javax.inject.Inject

/**
 * Вход по отпечатку на уровне PIN.
 *
 * Включение — PIN (достать ключ данных) и системный запрос биометрии с шифром: только после него
 * Keystore позволит завернуть ключ биометрическим ключом. Сессия живёт здесь, экран получает
 * только шифр для `CryptoObject`.
 */
@HiltViewModel
class BiometricSettingsViewModel @Inject constructor(
    private val biometrics: LocalDataBiometrics
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityFlowState())
    val state: StateFlow<SecurityFlowState> = _state.asStateFlow()

    private val _events = Channel<SecurityEvent>(Channel.BUFFERED)

    /** Одноразовые события: системный запрос биометрии. */
    val events: Flow<SecurityEvent> = _events.receiveAsFlow()

    private var enrollment: BiometricSession? = null

    /** Переключатель входа по отпечатку. */
    fun onToggled(enabled: Boolean) {
        if (enabled) {
            _state.update { it.copy(pinPrompt = PinPrompt(PinPurpose.EnableBiometric)) }
        } else {
            viewModelScope.launch { biometrics.disable() }
        }
    }

    /** Включить отпечаток уже подтверждённым PIN (сразу после включения уровня PIN). */
    fun enroll(pin: String) {
        _state.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            val message = when (val start = biometrics.startEnrollment(pin)) {
                is BiometricEnrollmentStart.Ready -> {
                    enrollment = start.session
                    _events.send(SecurityEvent.ShowBiometricPrompt(start.session.cipher))
                    null
                }

                BiometricEnrollmentStart.WrongPin -> SecurityMessage.WRONG_PIN

                BiometricEnrollmentStart.Unavailable -> SecurityMessage.BIOMETRIC_FAILED
            }
            _state.update { it.copy(inProgress = false, message = message) }
        }
    }

    /** Введён PIN для включения. */
    fun onPinEntered(pin: String) {
        if (_state.value.pinPrompt == null) return
        _state.update { it.copy(pinPrompt = null) }
        enroll(pin)
    }

    /** Ввод PIN закрыт. */
    fun onDismissed() {
        _state.update { it.copy(pinPrompt = null) }
    }

    /** Системный запрос биометрии завершился успешно — шифр разблокирован. */
    fun onPromptSucceeded() {
        val session = enrollment ?: return
        enrollment = null
        _state.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            val message = if (biometrics.finishEnrollment(session)) {
                SecurityMessage.BIOMETRIC_ENABLED
            } else {
                SecurityMessage.BIOMETRIC_FAILED
            }
            _state.update { it.copy(inProgress = false, message = message) }
        }
    }

    /** Системный запрос отменён или не удался: несохранённый ключ удаляется. */
    fun onPromptFailed() {
        val session = enrollment ?: return
        enrollment = null
        viewModelScope.launch { biometrics.cancel(session) }
    }

    /** Сообщение показано. */
    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }
}
