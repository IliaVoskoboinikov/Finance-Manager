package soft.divan.financemanager.feature.haptics.impl.data.haptics

import android.os.VibrationEffect
import android.os.Vibrator
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HapticsManagerImplTest {

    private val vibrator = mockk<Vibrator>(relaxUnitFun = true)
    private val observeHapticsEnabledUseCase = mockk<ObserveHapticsEnabledUseCase>()
    private val enabledFlow = MutableStateFlow(true)
    private val oneShotEffect = mockk<VibrationEffect>()
    private val waveformEffect = mockk<VibrationEffect>()

    @Before
    fun setUp() {
        mockkStatic(VibrationEffect::class)
        every { VibrationEffect.createOneShot(any(), any()) } returns oneShotEffect
        every { VibrationEffect.createWaveform(any(), any<IntArray>(), any()) } returns
            waveformEffect
        every { observeHapticsEnabledUseCase() } returns enabledFlow
        every { vibrator.hasVibrator() } returns true
    }

    @After
    fun tearDown() {
        unmockkStatic(VibrationEffect::class)
    }

    private fun manager() = HapticsManagerImpl(
        vibrator = vibrator,
        observeHapticEnabled = observeHapticsEnabledUseCase,
        scope = CoroutineScope(UnconfinedTestDispatcher())
    )

    @Test
    fun `one-shot haptic types vibrate with one-shot effect`() = runTest {
        val manager = manager()

        manager.perform(HapticType.CLICK)
        manager.perform(HapticType.TOGGLE)
        manager.perform(HapticType.LONG_PRESS)

        verify(exactly = 3) { vibrator.vibrate(oneShotEffect) }
    }

    @Test
    fun `waveform haptic types vibrate with waveform effect`() = runTest {
        val manager = manager()

        manager.perform(HapticType.SUCCESS)
        manager.perform(HapticType.ERROR)

        verify(exactly = 2) { vibrator.vibrate(waveformEffect) }
    }

    @Test
    fun `perform does nothing when haptics are disabled in settings`() = runTest {
        enabledFlow.value = false
        val manager = manager()

        manager.perform(HapticType.CLICK)

        verify(exactly = 0) { vibrator.vibrate(any<VibrationEffect>()) }
    }

    @Test
    fun `perform does nothing when device has no vibrator`() = runTest {
        every { vibrator.hasVibrator() } returns false
        val manager = manager()

        manager.perform(HapticType.CLICK)

        verify(exactly = 0) { vibrator.vibrate(any<VibrationEffect>()) }
    }

    @Test
    fun `manager reacts to settings changes`() = runTest {
        val manager = manager()

        manager.perform(HapticType.CLICK)
        enabledFlow.value = false
        manager.perform(HapticType.CLICK)

        verify(exactly = 1) { vibrator.vibrate(oneShotEffect) }
    }
}
