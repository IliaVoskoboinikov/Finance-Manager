package soft.divan.financemanager.feature.sounds.impl.data.sounds

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.feature.sounds.api.domain.SoundPlayer
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import javax.inject.Inject

class SoundPlayerImpl @Inject constructor(
    private val soundsPoolHolder: SoundsPoolHolder,
    observeSoundEnabled: ObserveSoundsEnabledUseCase,
    @ApplicationScope scope: CoroutineScope
) : SoundPlayer {

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
// Revue me>>
