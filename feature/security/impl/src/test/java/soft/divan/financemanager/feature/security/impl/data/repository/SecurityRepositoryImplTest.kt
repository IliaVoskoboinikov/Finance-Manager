package soft.divan.financemanager.feature.security.impl.data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.data.crypto.PinHasher
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource

class SecurityRepositoryImplTest {

    private val dataSource = mockk<SecurityLocalDataSource>(relaxed = true)
    private val repository = SecurityRepositoryImpl(dataSource)

    @Before
    fun setup() {
        mockkObject(PinHasher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `savePin stores the hashed pin, never the raw pin`() {
        every { PinHasher.hash(PIN) } returns HASH

        repository.savePin(PIN)

        verify { dataSource.savePin(HASH) }
        verify(exactly = 0) { dataSource.savePin(PIN) }
    }

    @Test
    fun `verifyPin returns false when no pin is stored`() {
        every { dataSource.getPin() } returns null

        assertThat(repository.verifyPin(PIN)).isFalse()
        verify(exactly = 0) { PinHasher.verify(any(), any()) }
    }

    @Test
    fun `verifyPin delegates to PinHasher with the stored hash`() {
        every { dataSource.getPin() } returns HASH
        every { PinHasher.verify(PIN, HASH) } returns true

        assertThat(repository.verifyPin(PIN)).isTrue()
        verify { PinHasher.verify(PIN, HASH) }
    }

    @Test
    fun `verifyPin returns false when PinHasher rejects the pin`() {
        every { dataSource.getPin() } returns HASH
        every { PinHasher.verify(any(), any()) } returns false

        assertThat(repository.verifyPin("0000")).isFalse()
    }

    @Test
    fun `isPinSet delegates to the data source`() {
        every { dataSource.isPinSet() } returns true

        assertThat(repository.isPinSet()).isTrue()
    }

    @Test
    fun `deletePin delegates to the data source`() {
        repository.deletePin()

        verify { dataSource.deletePin() }
    }

    private companion object {
        const val PIN = "1234"
        const val HASH = "salt:hash"
    }
}
