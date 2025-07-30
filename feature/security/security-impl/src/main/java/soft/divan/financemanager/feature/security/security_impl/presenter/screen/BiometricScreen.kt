package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import soft.divan.financemanager.feature.security.security_impl.presenter.util.BiometricHelper


/**
 * Field requires API level 28 (current min is 24): android
 */
@Composable
@RequiresApi(Build.VERSION_CODES.P)
fun BiometricScreen(authenticationCallback: BiometricPrompt.AuthenticationCallback) {
    val context = LocalContext.current
    val biometricHelper = BiometricHelper(context)

    /**
     * The process of creating the requestPermissionLauncher that is used to request permission
     */
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            biometricHelper.openBiometric(authenticationCallback)
        } else {
            /**
             * Perform actions if not granted
             */
        }
    }

    when (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.USE_BIOMETRIC
    )) {
        PackageManager.PERMISSION_GRANTED -> {
            biometricHelper.openBiometric(authenticationCallback)
        }

        else -> {
            /**
             * Request permission if not granted
             */
            requestPermissionLauncher.launch(Manifest.permission.USE_BIOMETRIC)
        }
    }
}