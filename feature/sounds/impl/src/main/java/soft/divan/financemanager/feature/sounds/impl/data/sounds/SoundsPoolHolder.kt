package soft.divan.financemanager.feature.sounds.impl.data.sounds

import android.content.Context
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.impl.R
import javax.inject.Inject

class SoundsPoolHolder @Inject constructor(
    @ApplicationContext context: Context
) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    private val sounds: Map<SoundType, Int> = mapOf(
        SoundType.COIN to soundPool.load(context, R.raw.coin, 1)
    )

    fun play(type: SoundType) {
        sounds[type]?.let {
            soundPool.play(it, 1f, 1f, 1, 0, 1f)
        }
    }
}
