package soft.divan.financemanager.feature.security.security_impl.presenter.util

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import soft.divan.financemanager.feature.security.security_impl.R
import java.util.concurrent.Executor

@RequiresApi(Build.VERSION_CODES.P)
class BiometricHelper(private val context: Context) {

    private var executor: Executor? = null

    //todo если на устройстве нет добавленных отпечатков пальца то это работать не будет
    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun startAuthentication(authenticationCallback: BiometricPrompt.AuthenticationCallback) {
        executor = ContextCompat.getMainExecutor(context)
        val cancellationSignal = CancellationSignal()
        val biometricPrompt = executor?.let {
            BiometricPrompt.Builder(context)
                .setTitle(context.getString(R.string.authorization))
                .setSubtitle(context.getString(R.string.use_biometry))
                .setDescription(context.getString(R.string.confirm_identity))
                .setNegativeButton(context.getString(R.string.cancel_action), it) { _, _ -> }
                .build()
        }

        executor?.let {
            biometricPrompt?.authenticate(
                cancellationSignal,
                it,
                authenticationCallback
            )
        }
    }

    fun openBiometric(authenticationCallback: BiometricPrompt.AuthenticationCallback) {

        if (isBiometricSupported()) {
            startAuthentication(authenticationCallback)
        } else {
            // Биометрическая аутентификация не поддерживается
        }
    }
}