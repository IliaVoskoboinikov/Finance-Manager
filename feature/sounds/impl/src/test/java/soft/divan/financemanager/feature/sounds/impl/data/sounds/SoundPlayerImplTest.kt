package soft.divan.financemanager.feature.sounds.impl.data.sounds

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class SoundPlayerImplTest {

    private val soundsPoolHolder = mockk<SoundsPoolHolder>(relaxUnitFun = true)
    private val observeSoundsEnabledUseCase = mockk<ObserveSoundsEnabledUseCase>()
    private val enabledFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        every { observeSoundsEnabledUseCase() } returns enabledFlow
    }

    private fun player() = SoundPlayerImpl(
        soundsPoolHolder = soundsPoolHolder,
        observeSoundEnabled = observeSoundsEnabledUseCase,
        scope = CoroutineScope(UnconfinedTestDispatcher())
    )

    @Test
    fun `play delegates to sound pool when enabled`() = runTest {
        player().play(SoundType.COIN)

        verify(exactly = 1) { soundsPoolHolder.play(SoundType.COIN) }
    }

    @Test
    fun `play is silent when sounds are disabled`() = runTest {
        enabledFlow.value = false

        player().play(SoundType.COIN)

        verify(exactly = 0) { soundsPoolHolder.play(any()) }
    }

    @Test
    fun `player reacts to settings changes`() = runTest {
        val player = player()

        player.play(SoundType.COIN)
        enabledFlow.value = false
        player.play(SoundType.COIN)

        verify(exactly = 1) { soundsPoolHolder.play(SoundType.COIN) }
    }
}
