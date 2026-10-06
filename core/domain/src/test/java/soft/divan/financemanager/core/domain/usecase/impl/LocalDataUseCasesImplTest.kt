package soft.divan.financemanager.core.domain.usecase.impl

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository

class LocalDataUseCasesImplTest {

    private val state = MutableStateFlow<LocalDataState>(LocalDataState.Locked(biometricEnabled = true))
    private val repository = mockk<LocalDataAccessRepository>(relaxUnitFun = true) {
        every { state } returns this@LocalDataUseCasesImplTest.state
    }

    @Test
    fun `state is observed straight from the repository`() {
        val observed = ObserveLocalDataStateUseCaseImpl(repository)()

        assertThat(observed.value).isEqualTo(LocalDataState.Locked(biometricEnabled = true))
        state.value = LocalDataState.Open
        assertThat(observed.value).isEqualTo(LocalDataState.Open)
    }

    @Test
    fun `initialize delegates to the repository`() = runTest {
        InitializeLocalDataUseCaseImpl(repository)()

        coVerify(exactly = 1) { repository.initialize() }
    }

    @Test
    fun `lock delegates to the repository`() = runTest {
        LockLocalDataUseCaseImpl(repository)()

        coVerify(exactly = 1) { repository.lock() }
    }

    @Test
    fun `lockout defaults to no pause`() {
        assertThat(PinLockout(attemptsLeft = 3).lockedUntil).isNull()
    }
}
