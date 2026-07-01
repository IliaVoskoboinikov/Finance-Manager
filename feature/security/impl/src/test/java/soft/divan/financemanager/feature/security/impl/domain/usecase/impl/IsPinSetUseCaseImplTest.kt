package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class IsPinSetUseCaseImplTest {

    private val repository = mockk<SecurityRepository>()
    private val useCase = IsPinSetUseCaseImpl(repository)

    @Test
    fun `invoke returns true when a pin is set`() {
        every { repository.isPinSet() } returns true

        assertThat(useCase()).isTrue()
    }

    @Test
    fun `invoke returns false when no pin is set`() {
        every { repository.isPinSet() } returns false

        assertThat(useCase()).isFalse()
    }
}
