package soft.divan.financemanager.core.security

import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import soft.divan.financemanager.core.security.impl.AndroidCryptoManager
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Provider

@RunWith(RobolectricTestRunner::class)
class AndroidCryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager
    private lateinit var mockKeyStore: KeyStore
    private lateinit var mockKeyGenerator: KeyGenerator
    private lateinit var mockCipher: Cipher
    private lateinit var mockSecretKey: SecretKey

    private val alias = "test_alias"
    private val plainText = "Hello financemanager!"

    @Before
    fun setup() {
        mockKeyStore = mockk(relaxed = true)
        mockKeyGenerator = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        mockSecretKey = mockk(relaxed = true)

        // Mocking the Builder to avoid "Method not mocked" errors in Robolectric
        mockkConstructor(KeyGenParameterSpec.Builder::class)
        every {
            anyConstructed<KeyGenParameterSpec.Builder>().setBlockModes(any())
        } returns mockk(relaxed = true)
        every {
            anyConstructed<KeyGenParameterSpec.Builder>().setEncryptionPaddings(any())
        } returns mockk(relaxed = true)
        every {
            anyConstructed<KeyGenParameterSpec.Builder>().setUserAuthenticationRequired(any())
        } returns mockk(relaxed = true)
        every {
            anyConstructed<KeyGenParameterSpec.Builder>().setRandomizedEncryptionRequired(any())
        } returns mockk(relaxed = true)
        every {
            anyConstructed<KeyGenParameterSpec.Builder>().setKeySize(
                any()
            )
        } returns mockk(relaxed = true)
        every { anyConstructed<KeyGenParameterSpec.Builder>().build() } returns mockk(relaxed = true)

        val keyGeneratorProvider = Provider { mockKeyGenerator }
        val cipherProvider = Provider { mockCipher }

        cryptoManager = AndroidCryptoManager(
            keyStore = mockKeyStore,
            keyGeneratorProvider = keyGeneratorProvider,
            cipherProvider = cipherProvider
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `encrypt should call cipher doFinal and return base64 string`() {
        // GCM: doFinal returns encrypted data + 16 bytes auth tag
        val encryptedWithTag = ByteArray(25) { 1 }
        val ivBytes = ByteArray(12) { 1 }

        every { mockKeyStore.getEntry(alias, null) } returns KeyStore.SecretKeyEntry(mockSecretKey)
        every { mockCipher.doFinal(any<ByteArray>()) } returns encryptedWithTag
        every { mockCipher.iv } returns ivBytes

        val result = cryptoManager.encrypt(plainText, alias)

        verify { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) }
        verify { mockCipher.doFinal(plainText.toByteArray(Charsets.UTF_8)) }
        val decoded = Base64.decode(result, Base64.DEFAULT)
        assertEquals(37, decoded.size) // 12 (iv) + 25 (encrypted + tag)
    }

    @Test
    fun `decrypt should extract IV and call cipher doFinal`() {
        val iv = ByteArray(12) { 2 }
        val encryptedWithTag = ByteArray(25) { 3 }
        val combined = iv + encryptedWithTag
        val base64Input = Base64.encodeToString(combined, Base64.DEFAULT)

        every { mockKeyStore.getEntry(alias, null) } returns KeyStore.SecretKeyEntry(mockSecretKey)
        every { mockCipher.doFinal(any()) } returns plainText.toByteArray(Charsets.UTF_8)

        val result = cryptoManager.decrypt(base64Input, alias)

        assertEquals(plainText, result)
        verify { mockCipher.init(Cipher.DECRYPT_MODE, mockSecretKey, any<GCMParameterSpec>()) }
        verify { mockCipher.doFinal(encryptedWithTag) }
    }

    @Test
    fun `decrypt should throw GeneralSecurityException when data is too short`() {
        val shortData = ByteArray(10) { 0 }
        val base64Input = Base64.encodeToString(shortData, Base64.DEFAULT)

        assertThrows(GeneralSecurityException::class.java) {
            cryptoManager.decrypt(base64Input, alias)
        }
    }

    @Test
    fun `getKey should recreate key if getEntry throws GeneralSecurityException`() {
        // Arrange
        every { mockKeyStore.getEntry(alias, null) } throws UnrecoverableKeyException("Corrupted")
        every { mockKeyGenerator.generateKey() } returns mockSecretKey
        every { mockCipher.doFinal(any<ByteArray>()) } returns "ok".toByteArray()

        // Act
        cryptoManager.encrypt(plainText, alias)

        // Assert
        verify { mockKeyStore.deleteEntry(alias) }
        verify { mockKeyGenerator.generateKey() }
    }

    @Test
    fun `getKey creates a new key when no secret key is stored`() {
        // getEntry вернул не SecretKeyEntry (null) → ветка createKey без пересоздания
        every { mockKeyStore.getEntry(alias, null) } returns null
        every { mockKeyGenerator.generateKey() } returns mockSecretKey
        every { mockCipher.doFinal(any<ByteArray>()) } returns "ok".toByteArray()

        cryptoManager.encrypt(plainText, alias)

        verify(exactly = 0) { mockKeyStore.deleteEntry(alias) }
        verify { mockKeyGenerator.generateKey() }
    }

    @Test
    fun `deleteEntry failure during recreate is swallowed`() {
        every { mockKeyStore.getEntry(alias, null) } throws UnrecoverableKeyException("Corrupted")
        every { mockKeyStore.deleteEntry(alias) } throws GeneralSecurityException("delete failed")
        every { mockKeyGenerator.generateKey() } returns mockSecretKey
        every { mockCipher.doFinal(any<ByteArray>()) } returns "ok".toByteArray()

        cryptoManager.encrypt(plainText, alias)

        verify { mockKeyGenerator.generateKey() }
    }

    @Test
    fun `createKey rethrows when key generation fails`() {
        every { mockKeyStore.getEntry(alias, null) } returns null
        every { mockKeyGenerator.generateKey() } throws GeneralSecurityException("no keystore")

        assertThrows(GeneralSecurityException::class.java) {
            cryptoManager.encrypt(plainText, alias)
        }
    }

    @Test
    fun `decrypt wraps invalid base64 into GeneralSecurityException`() {
        assertThrows(GeneralSecurityException::class.java) {
            cryptoManager.decrypt("!!!not-base64!!!", alias)
        }
    }
}
