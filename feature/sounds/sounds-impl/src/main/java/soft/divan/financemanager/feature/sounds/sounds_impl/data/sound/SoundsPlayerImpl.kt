package soft.divan.financemanager.feature.sounds.sounds_impl.data.sound

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import soft.divan.financemanager.feature.sounds.sounds_api.domain.SoundType
import soft.divan.financemanager.feature.sounds.sounds_api.domain.SoundsPlayer
import soft.divan.financemanager.feature.sounds.sounds_impl.di.ApplicationScope
import soft.divan.financemanager.feature.sounds.sounds_impl.domain.usecase.ObserveSoundsEnabledUseCase
import javax.inject.Inject

class SoundsPlayerImpl @Inject constructor(
    private val soundsPoolHolder: SoundsPoolHolder,
    observeSoundEnabled: ObserveSoundsEnabledUseCase,
    @ApplicationScope scope: CoroutineScope
) : SoundsPlayer {

    @Volatile
    private var enabled: Boolean = true

    init {
        observeSoundEnabled()
            .onEach { enabled = it }
            .launchIn(scope)
    }

    override fun play(soundType: SoundType) {
        if (!enabled) return
        soundsPoolHolder.play(soundType)
    }
}