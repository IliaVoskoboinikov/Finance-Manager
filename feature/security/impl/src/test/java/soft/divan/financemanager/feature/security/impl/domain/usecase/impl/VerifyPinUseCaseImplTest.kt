package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class VerifyPinUseCaseImplTest {

    private val repository = mockk<SecurityRepository>()
    private val useCase = VerifyPinUseCaseImpl(repository)

    @Test
    fun `invoke returns true and delegates to repository`() {
        every { repository.verifyPin("1234") } returns true

        assertThat(useCase("1234")).isTrue()
        verify { repository.verifyPin("1234") }
    }

    @Test
    fun `invoke returns false when repository rejects the pin`() {
        every { repository.verifyPin(any()) } returns false

        assertThat(useCase("0000")).isFalse()
    }
}
