package soft.divan.financemanager.feature.haptics.impl.data.repository

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.haptics.impl.data.source.HapticsLocalSource

class HapticsRepositoryImplTest {

    private val localSource = mockk<HapticsLocalSource>(relaxUnitFun = true)
    private val repository = HapticsRepositoryImpl(localSource)

    @Test
    fun `observeHapticsEnabled delegates to local source`() = runTest {
        every { localSource.getHapticsEnabled() } returns flowOf(false)

        assertThat(repository.observeHapticsEnabled().first()).isFalse()
    }

    @Test
    fun `setHapticsEnabled delegates to local source`() = runTest {
        repository.setHapticsEnabled(true)

        coVerify(exactly = 1) { localSource.setHapticsEnabled(true) }
    }
}
