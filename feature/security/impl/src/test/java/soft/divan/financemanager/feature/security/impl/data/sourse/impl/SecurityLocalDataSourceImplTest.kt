package soft.divan.financemanager.feature.security.impl.data.sourse.impl

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import soft.divan.financemanager.core.security.CryptoManager

@RunWith(RobolectricTestRunner::class)
class SecurityLocalDataSourceImplTest {

    private val cryptoManager = mockk<CryptoManager>()
    private val dataSource = SecurityLocalDataSourceImpl(
        RuntimeEnvironment.getApplication(),
        cryptoManager
    )

    @Test
    fun `savePin encrypts the value and isPinSet becomes true`() {
        every { cryptoManager.encrypt(HASH, any()) } returns ENCRYPTED

        dataSource.savePin(HASH)

        assertThat(dataSource.isPinSet()).isTrue()
    }

    @Test
    fun `getPin returns the decrypted value`() {
        every { cryptoManager.encrypt(HASH, any()) } returns ENCRYPTED
        every { cryptoManager.decrypt(ENCRYPTED, any()) } returns HASH
        dataSource.savePin(HASH)

        assertThat(dataSource.getPin()).isEqualTo(HASH)
    }

    @Test
    fun `getPin returns null when nothing is stored`() {
        assertThat(dataSource.getPin()).isNull()
        assertThat(dataSource.isPinSet()).isFalse()
    }

    @Test
    fun `getPin returns null when decryption fails`() {
        every { cryptoManager.encrypt(any(), any()) } returns ENCRYPTED
        every { cryptoManager.decrypt(any(), any()) } throws RuntimeException("decrypt failed")
        dataSource.savePin(HASH)

        assertThat(dataSource.getPin()).isNull()
    }

    @Test
    fun `deletePin clears the stored pin`() {
        every { cryptoManager.encrypt(any(), any()) } returns ENCRYPTED
        dataSource.savePin(HASH)

        dataSource.deletePin()

        assertThat(dataSource.isPinSet()).isFalse()
    }

    private companion object {
        const val HASH = "salt:hash"
        const val ENCRYPTED = "encrypted-blob"
    }
}
