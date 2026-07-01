package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class DeletePinUseCaseImplTest {

    private val repository = mockk<SecurityRepository>(relaxed = true)
    private val useCase = DeletePinUseCaseImpl(repository)

    @Test
    fun `invoke deletes the pin via repository`() {
        useCase()

        verify { repository.deletePin() }
    }
}
