package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangeProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecuritySettingsUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.SetSecureScreenUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityDialog
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState
import javax.inject.Inject

/**
 * Экран настроек безопасности: что настроено, смена уровня защиты данных и защита экрана.
 *
 * Смена уровня многошаговая — предупреждение, ввод PIN, иногда предложение отпечатка, — поэтому
 * поверх наблюдаемых настроек здесь живёт состояние диалога ([Interaction]). Смену PIN и
 * биометрию ведут [PinSettingsViewModel] и [BiometricSettingsViewModel].
 */
@HiltViewModel
class SecurityViewModel @Inject constructor(
    observeSettings: ObserveSecuritySettingsUseCase,
    private val changeProtectionLevel: ChangeProtectionLevelUseCase,
    private val setSecureScreen: SetSecureScreenUseCase
) : ViewModel() {

    private val interaction = MutableStateFlow(Interaction())
    private var settings: SecuritySettings? = null

    val uiState: StateFlow<SecurityUiState> =
        combine(observeSettings(), interaction) { current, dialogState ->
            settings = current
            SecurityUiState.Success(
                settings = current,
                biometricAvailable = dialogState.biometricAvailable,
                dialog = dialogState.dialog,
                pinPrompt = dialogState.pinPrompt,
                inProgress = dialogState.inProgress,
                message = dialogState.message
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            SecurityUiState.Loading
        )

    private val _events = Channel<SecurityEvent>(Channel.BUFFERED)

    /** Одноразовые события: включить отпечаток после включения уровня PIN. */
    val events: Flow<SecurityEvent> = _events.receiveAsFlow()

    /** PIN, которым только что включили уровень, — чтобы не спрашивать его второй раз для отпечатка. */
    private var offerPin: String? = null

    /** Есть ли на устройстве строгая биометрия — знает только экран. */
    fun onBiometricAvailability(available: Boolean) {
        interaction.update { it.copy(biometricAvailable = available) }
    }

    /** Выбран уровень защиты. */
    fun onLevelSelected(target: DataProtectionLevel) {
        val current = settings ?: return
        when {
            current.level == target -> Unit

            // Повышение защиты без PIN ни о чём не предупреждает — просто выполняется
            current.level == DataProtectionLevel.NONE && target == DataProtectionLevel.DEVICE ->
                applyLevel(target, pin = null)

            else -> interaction.update { it.copy(dialog = SecurityDialog.ConfirmLevel(target)) }
        }
    }

    /** Предупреждение о смене уровня принято. */
    fun onLevelConfirmed(target: DataProtectionLevel) {
        val current = settings ?: return
        val prompt = when {
            target == DataProtectionLevel.PIN -> PinPrompt(
                PinPurpose.EnablePinLevel,
                create = !current.hasPin
            )

            current.level == DataProtectionLevel.PIN -> PinPrompt(PinPurpose.LeavePinLevel(target))

            else -> null
        }
        interaction.update { it.copy(dialog = null, pinPrompt = prompt) }
        if (prompt == null) applyLevel(target, pin = null)
    }

    /** Введён PIN для смены уровня. */
    fun onPinEntered(pin: String) {
        val prompt = interaction.value.pinPrompt ?: return
        interaction.update { it.copy(pinPrompt = null) }
        when (val purpose = prompt.purpose) {
            PinPurpose.EnablePinLevel -> applyLevel(
                DataProtectionLevel.PIN,
                pin,
                isNewPin = prompt.create
            )

            is PinPurpose.LeavePinLevel -> applyLevel(purpose.target, pin)

            else -> Unit
        }
    }

    /** Закрыт диалог или ввод PIN. */
    fun onDismissed() {
        offerPin = null
        interaction.update { it.copy(dialog = null, pinPrompt = null) }
    }

    /** Предложение включить отпечаток после включения уровня PIN принято. */
    fun onBiometricOfferAccepted() {
        val pin = offerPin ?: return
        offerPin = null
        interaction.update { it.copy(dialog = null) }
        viewModelScope.launch { _events.send(SecurityEvent.EnrollBiometric(pin)) }
    }

    /** Переключатель защиты экрана. */
    fun onSecureScreenToggled(enabled: Boolean) {
        viewModelScope.launch { setSecureScreen(enabled) }
    }

    /** Сообщение показано. */
    fun onMessageShown() {
        interaction.update { it.copy(message = null) }
    }

    private fun applyLevel(target: DataProtectionLevel, pin: String?, isNewPin: Boolean = false) {
        interaction.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            val result = changeProtectionLevel(target, pin, isNewPin)
            interaction.update { state ->
                val next = state.copy(inProgress = false)
                when (result) {
                    ProtectionChangeResult.Changed -> onLevelChanged(next, target, pin)

                    ProtectionChangeResult.WrongPin -> next.copy(message = SecurityMessage.WRONG_PIN)

                    ProtectionChangeResult.UnsentChanges -> next.copy(
                        dialog = SecurityDialog.UnsentChanges
                    )

                    ProtectionChangeResult.Failed -> next.copy(message = SecurityMessage.CHANGE_FAILED)
                }
            }
        }
    }

    private fun onLevelChanged(
        state: Interaction,
        target: DataProtectionLevel,
        pin: String?
    ): Interaction =
        if (target == DataProtectionLevel.PIN && state.biometricAvailable && pin != null) {
            offerPin = pin
            state.copy(dialog = SecurityDialog.OfferBiometric)
        } else {
            state.copy(message = SecurityMessage.LEVEL_CHANGED)
        }

    /** Что открыто поверх настроек. */
    private data class Interaction(
        val biometricAvailable: Boolean = false,
        val dialog: SecurityDialog? = null,
        val pinPrompt: PinPrompt? = null,
        val inProgress: Boolean = false,
        val message: SecurityMessage? = null
    )
}
