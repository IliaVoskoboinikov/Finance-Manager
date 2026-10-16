package soft.divan.financemanager.feature.security.impl.presenter.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/**
 * Системный `BiometricPrompt` (androidx) — с шифром или без.
 *
 * - **С шифром** ([Cipher] в `CryptoObject`) — только строгая биометрия (Class 3): лишь она
 *   разблокирует ключ Keystore, и именно Keystore, а не этот класс, решает, выдать ли ключ.
 *   Так открываются данные уровня PIN.
 * - **Без шифра** — любая биометрия; это только замок интерфейса ниже уровня PIN.
 *
 * Прежний `BiometricHelper` вызывал платформенный `android.hardware.biometrics.BiometricPrompt`
 * без `CryptoObject`: биометрия лишь разрешала показать экран, ключа она не давала.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    /** Есть ли на устройстве настроенная строгая биометрия. */
    fun canUseStrong(): Boolean = canAuthenticate(BIOMETRIC_STRONG)

    /** Есть ли любая настроенная биометрия. */
    fun canUseAny(): Boolean = canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

    /**
     * Показывает запрос. [onSuccess] получает разблокированный шифр (или `null` без шифра);
     * [onFailure] — отмена, «ввести PIN» или ошибка. Непохожий палец не завершает запрос —
     * система сама предложит попробовать ещё раз.
     */
    fun authenticate(
        texts: BiometricTexts,
        cipher: Cipher?,
        onSuccess: (Cipher?) -> Unit,
        onFailure: () -> Unit
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result.cryptoObject?.cipher)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure()
            }
        }
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(texts.title)
            .setSubtitle(texts.subtitle)
            .setNegativeButtonText(texts.negativeButton)
            .setAllowedAuthenticators(
                if (cipher != null) BIOMETRIC_STRONG else BIOMETRIC_STRONG or BIOMETRIC_WEAK
            )
            .setConfirmationRequired(false)
            .build()
        if (cipher != null) {
            prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
        } else {
            prompt.authenticate(info)
        }
    }

    private fun canAuthenticate(authenticators: Int): Boolean =
        BiometricManager.from(
            activity
        ).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
}

/** Тексты системного запроса биометрии. */
data class BiometricTexts(
    val title: String,
    val subtitle: String,
    val negativeButton: String
)

/** Activity, на которой можно показать `BiometricPrompt`, или `null`, если её нет в цепочке. */
fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
