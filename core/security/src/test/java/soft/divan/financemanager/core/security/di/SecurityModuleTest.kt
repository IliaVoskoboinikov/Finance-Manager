package soft.divan.financemanager.core.security.di

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException

class SecurityModuleTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `provideAesCipher creates AES GCM cipher`() {
        val cipher = SecurityModule.provideAesCipher()

        assertThat(cipher.algorithm).isEqualTo(SecurityModule.AES_TRANSFORMATION)
    }

    @Test
    fun `aes transformation constant matches project crypto scheme`() {
        assertThat(SecurityModule.AES_TRANSFORMATION).isEqualTo("AES/GCM/NoPadding")
    }

    @Test
    fun `provideKeyStore rethrows when AndroidKeyStore is unavailable`() {
        // На JVM провайдера AndroidKeyStore нет — модуль должен логировать и пробрасывать
        // ошибку, а не глотать её
        assertThatThrownBy { SecurityModule.provideKeyStore() }
            .isInstanceOf(GeneralSecurityException::class.java)
    }

    @Test
    fun `provideKeyGenerator rethrows when AndroidKeyStore is unavailable`() {
        assertThatThrownBy { SecurityModule.provideKeyGenerator() }
            .isInstanceOf(GeneralSecurityException::class.java)
    }
}
