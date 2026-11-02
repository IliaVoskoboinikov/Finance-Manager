package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class ObservePinSetUseCaseImplTest {

    private val repository = mockk<SecurityRepository>()
    private val useCase = ObservePinSetUseCaseImpl(repository, UnconfinedTestDispatcher())

    @Test
    fun `pin removal reaches the observer`() = runTest {
        // Стирание данных снимает PIN — замок должен узнать об этом, а не держать старое значение
        every { repository.observePinSet() } returns flowOf(true, false)

        assertThat(useCase().toList()).containsExactly(true, false)
    }
}
