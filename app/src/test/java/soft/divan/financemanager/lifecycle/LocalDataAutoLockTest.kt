package soft.divan.financemanager.lifecycle

import androidx.lifecycle.LifecycleOwner
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.usecase.LockLocalDataUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataAutoLockTest {

    private val lock = mockk<LockLocalDataUseCase>(relaxUnitFun = true)
    private val owner = mockk<LifecycleOwner>()
    private var elapsed = 0L

    private fun TestScope.autoLock() = LocalDataAutoLock(lock, backgroundScope, DELAY, { elapsed })

    @Test
    fun `long stay in background closes the data`() = runTest {
        val autoLock = autoLock()

        autoLock.onStop(owner)
        advanceTimeBy(DELAY - 1.seconds)
        coVerify(exactly = 0) { lock() }

        advanceTimeBy(2.seconds)
        coVerify(exactly = 1) { lock() }
    }

    @Test
    fun `coming back in time keeps the data open`() = runTest {
        val autoLock = autoLock()

        autoLock.onStop(owner)
        advanceTimeBy(DELAY / 2)
        elapsed += (DELAY / 2).inWholeMilliseconds
        autoLock.onStart(owner)
        advanceTimeBy(DELAY)

        coVerify(exactly = 0) { lock() }
    }

    @Test
    fun `frozen process closes the data on return when the time is up`() = runTest {
        val autoLock = autoLock()

        autoLock.onStop(owner)
        // Процесс заморожен: таймер не шёл, а реальное время — да
        elapsed += (DELAY + 1.milliseconds).inWholeMilliseconds
        autoLock.onStart(owner)
        runCurrent()

        coVerify(exactly = 1) { lock() }
    }

    @Test
    fun `start without a stop does nothing`() = runTest {
        autoLock().onStart(owner)
        runCurrent()

        coVerify(exactly = 0) { lock() }
    }

    @Test
    fun `repeated stops keep a single timer`() = runTest {
        val autoLock = autoLock()

        autoLock.onStop(owner)
        autoLock.onStop(owner)
        advanceTimeBy(DELAY + 1.seconds)

        coVerify(exactly = 1) { lock() }
    }

    @Test
    fun `production delay is five minutes`() {
        assertThat(LocalDataAutoLock.AUTO_LOCK_DELAY).isEqualTo(5.minutes)
    }

    private companion object {
        val DELAY = 5.minutes
    }
}
