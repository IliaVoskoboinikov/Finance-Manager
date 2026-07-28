package soft.divan.financemanager.feature.security.impl.presenter.util

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.security.impl.presenter.util.Dimens

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BiometricHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        unmockkStatic(BiometricManager::class)
    }

    @Test
    fun `openBiometric is silent when biometrics are not supported`() {
        mockkStatic(BiometricManager::class)
        val biometricManager = mockk<BiometricManager> {
            every { canAuthenticate() } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        }
        every { BiometricManager.from(context) } returns biometricManager
        val callback = mockk<BiometricPrompt.AuthenticationCallback>(relaxed = true)

        BiometricHelper(context).openBiometric(callback)
    }

    @Test
    fun `openBiometric starts authentication when biometrics are available`() {
        mockkStatic(BiometricManager::class)
        val biometricManager = mockk<BiometricManager> {
            every { canAuthenticate() } returns BiometricManager.BIOMETRIC_SUCCESS
        }
        every { BiometricManager.from(context) } returns biometricManager
        val callback = mockk<BiometricPrompt.AuthenticationCallback>(relaxed = true)

        BiometricHelper(context).openBiometric(callback)
    }

    @Test
    fun `keyboard dimensions are positive`() {
        assertThat(Dimens.verticalKeyboardButtonPadding.value).isGreaterThan(0f)
        assertThat(Dimens.horizontalKeyboardButtonPadding.value).isGreaterThan(0f)
        assertThat(Dimens.keyBoardButtonSize.value).isGreaterThan(0f)
        assertThat(Dimens.keyBoardButtonFontSize.value).isGreaterThan(0f)
    }
}
