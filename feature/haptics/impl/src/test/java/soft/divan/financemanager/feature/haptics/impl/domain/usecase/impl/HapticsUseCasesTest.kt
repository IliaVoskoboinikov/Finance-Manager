package soft.divan.financemanager.feature.haptics.impl.domain.usecase.impl

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.haptics.impl.domain.repository.HapticsRepository

class HapticsUseCasesTest {

    private val repository = mockk<HapticsRepository>(relaxUnitFun = true)

    @Test
    fun `ObserveHapticsEnabledUseCase delegates to repository`() = runTest {
        every { repository.observeHapticsEnabled() } returns flowOf(true)

        assertThat(ObserveHapticsEnabledUseCaseImpl(repository)().first()).isTrue()
    }

    @Test
    fun `SetHapticsEnabledUseCase delegates to repository`() = runTest {
        SetHapticsEnabledUseCaseImpl(repository)(false)

        coVerify(exactly = 1) { repository.setHapticsEnabled(false) }
    }
}
