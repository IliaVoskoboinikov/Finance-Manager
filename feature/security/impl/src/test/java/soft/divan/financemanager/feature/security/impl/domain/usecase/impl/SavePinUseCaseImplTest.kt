package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.util.concurrent.Executors

class SavePinUseCaseImplTest {

    private val repository = mockk<SecurityRepository>()
    private val io: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor { Thread(it, IO_THREAD) }.asCoroutineDispatcher()
    private val useCase = SavePinUseCaseImpl(repository, io)

    @After
    fun tearDown() {
        io.close()
    }

    @Test
    fun `invoke saves the pin via repository`() = runTest {
        every { repository.savePin(PIN) } returns Unit

        assertThat(useCase(PIN)).isEqualTo(DomainResult.Success(Unit))
        verify(exactly = 1) { repository.savePin(PIN) }
    }

    @Test
    fun `pin is hashed on the io dispatcher, not on the caller thread`() = runTest {
        var savedOn: String? = null
        every { repository.savePin(PIN) } answers { savedOn = Thread.currentThread().name }

        useCase(PIN)

        assertThat(savedOn).isEqualTo(IO_THREAD)
    }

    @Test
    fun `keystore refusal becomes a failure instead of a crash`() = runTest {
        val cause = GeneralSecurityException("keystore")
        every { repository.savePin(PIN) } throws cause

        assertThat(useCase(PIN)).isEqualTo(DomainResult.Failure(DomainError.Unknown(cause)))
    }

    @Test
    fun `android keystore provider error becomes a failure instead of a crash`() = runTest {
        val cause = ProviderException("tee")
        every { repository.savePin(PIN) } throws cause

        assertThat(useCase(PIN)).isEqualTo(DomainResult.Failure(DomainError.Unknown(cause)))
    }

    @Test
    fun `unrelated errors are not turned into a failure`() = runTest {
        every { repository.savePin(PIN) } throws IllegalStateException("bug")

        assertThatThrownBy { runBlocking { useCase(PIN) } }
            .isInstanceOf(IllegalStateException::class.java)
    }

    private companion object {
        const val PIN = "1234"
        const val IO_THREAD = "test-io"
    }
}
