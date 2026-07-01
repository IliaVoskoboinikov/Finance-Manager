package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class SavePinUseCaseImplTest {

    private val repository = mockk<SecurityRepository>(relaxed = true)
    private val useCase = SavePinUseCaseImpl(repository)

    @Test
    fun `invoke saves the pin via repository`() {
        useCase("1234")

        verify { repository.savePin("1234") }
    }
}
