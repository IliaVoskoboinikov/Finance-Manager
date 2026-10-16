package soft.divan.financemanager.feature.security.impl.presenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import javax.crypto.Cipher

@Immutable
sealed interface SecurityUiState {
    data object Loading : SecurityUiState

    /**
     * @property settings Что сейчас настроено.
     * @property biometricAvailable На устройстве есть строгая биометрия — можно предлагать её.
     * @property dialog Открытый диалог-предупреждение.
     * @property pinPrompt Открытый ввод PIN.
     * @property inProgress Идёт перезаворачивание ключа или досылка изменений.
     * @property message Сообщение об итоге операции.
     */
    data class Success(
        val settings: SecuritySettings,
        val biometricAvailable: Boolean = false,
        val dialog: SecurityDialog? = null,
        val pinPrompt: PinPrompt? = null,
        val inProgress: Boolean = false,
        val message: SecurityMessage? = null
    ) : SecurityUiState

    data class Error(@field:StringRes val messageRes: Int) : SecurityUiState
}

/** Диалоги экрана настроек безопасности. */
@Immutable
sealed interface SecurityDialog {

    /** Предупреждение перед сменой уровня на [target]. */
    data class ConfirmLevel(val target: DataProtectionLevel) : SecurityDialog

    /** Уровень PIN не включён: остались неотправленные изменения. */
    data object UnsentChanges : SecurityDialog

    /** Уровень PIN включён — предложить вход по отпечатку. */
    data object OfferBiometric : SecurityDialog
}

/**
 * Ввод PIN поверх экрана.
 *
 * @property purpose Зачем спрашиваем.
 * @property create Придумать новый PIN (ввод и повтор), а не подтвердить текущий.
 */
@Immutable
data class PinPrompt(val purpose: PinPurpose, val create: Boolean = false) {

    /** Заголовок ввода. */
    @get:StringRes
    val titleRes: Int
        get() = when {
            purpose is PinPurpose.ChangePinNew -> R.string.come_up_with_new_pin
            create -> R.string.сome_up_with_pin
            else -> R.string.enter_current_pin
        }
}

/** Зачем спрашивается PIN. */
@Immutable
sealed interface PinPurpose {

    /** Включить уровень PIN. */
    data object EnablePinLevel : PinPurpose

    /** Уйти с уровня PIN на [target]. */
    data class LeavePinLevel(val target: DataProtectionLevel) : PinPurpose

    /** Сменить PIN: сначала текущий… */
    data object ChangePinCurrent : PinPurpose

    /** …затем новый. */
    class ChangePinNew(val currentPin: String) : PinPurpose

    /** Удалить PIN. */
    data object DeletePin : PinPurpose

    /** Включить вход по отпечатку. */
    data object EnableBiometric : PinPurpose
}

/** Итог операции — короткое сообщение внизу экрана. */
enum class SecurityMessage(@field:StringRes val textRes: Int) {
    LEVEL_CHANGED(R.string.message_level_changed),
    WRONG_PIN(R.string.wrong_pin),
    CHANGE_FAILED(R.string.message_change_failed),
    PIN_CHANGED(R.string.message_pin_changed),
    PIN_DELETED(R.string.message_pin_deleted),
    DELETE_NOT_ALLOWED(R.string.message_delete_not_allowed),
    BIOMETRIC_ENABLED(R.string.message_biometric_enabled),
    BIOMETRIC_FAILED(R.string.message_biometric_failed)
}

/**
 * Состояние вспомогательной операции экрана настроек (смена PIN, включение биометрии).
 *
 * @property pinPrompt Открытый ввод PIN.
 * @property inProgress Операция выполняется.
 * @property message Сообщение об итоге.
 */
@Immutable
data class SecurityFlowState(
    val pinPrompt: PinPrompt? = null,
    val inProgress: Boolean = false,
    val message: SecurityMessage? = null
)

/** Одноразовые события экрана настроек. */
sealed interface SecurityEvent {

    /** Уровень PIN включён, пользователь согласился на отпечаток — [pin] уже подтверждён. */
    class EnrollBiometric(val pin: String) : SecurityEvent

    /** Показать запрос биометрии, чтобы завернуть ключ биометрическим ключом. */
    class ShowBiometricPrompt(val cipher: Cipher) : SecurityEvent
}
