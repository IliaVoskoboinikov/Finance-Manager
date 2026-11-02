package soft.divan.financemanager.feature.sounds.impl.data.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.impl.R
import javax.inject.Inject

/**
 * Держит [SoundPool] со звуками приложения и проигрывает их по [SoundType].
 *
 * Звуки — обратная связь интерфейса, поэтому пул играет их как sonification
 * (поток `STREAM_SYSTEM`): система глушит его в беззвучном и вибро-режиме, а громкость
 * медиа на него не влияет.
 *
 * [SoundPool.load] асинхронный: `play` до окончания загрузки молча ничего не делает.
 * Поэтому запрос на ещё не загруженный звук откладывается и выполняется по
 * [SoundPool.OnLoadCompleteListener].
 */
class SoundsPoolHolder internal constructor(
    context: Context,
    private val soundPool: SoundPool
) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(context, createSoundPool())

    private val lock = Any()
    private val loadedSampleIds = mutableSetOf<Int>()
    private val pendingSampleIds = mutableSetOf<Int>()

    private val sounds: Map<SoundType, Int>

    init {
        // Слушатель ставится до load, иначе событие о загрузке можно пропустить.
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            onLoadComplete(sampleId, isSuccess = status == LOAD_STATUS_SUCCESS)
        }
        sounds = mapOf(
            SoundType.COIN to soundPool.load(context, R.raw.coin, LOAD_PRIORITY)
        )
    }

    /**
     * Проигрывает звук [type]. Если он ещё загружается — проиграет сразу после загрузки;
     * повторные запросы за это время схлопываются в один.
     */
    fun play(type: SoundType) {
        val sampleId = sounds[type] ?: return
        val isLoaded = synchronized(lock) {
            (sampleId in loadedSampleIds).also { loaded ->
                if (!loaded) pendingSampleIds += sampleId
            }
        }
        if (isLoaded) playSample(sampleId)
    }

    private fun onLoadComplete(sampleId: Int, isSuccess: Boolean) {
        val wasPending = synchronized(lock) {
            if (isSuccess) loadedSampleIds += sampleId
            pendingSampleIds.remove(sampleId)
        }
        if (isSuccess && wasPending) playSample(sampleId)
    }

    private fun playSample(sampleId: Int) {
        soundPool.play(sampleId, VOLUME, VOLUME, PLAY_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    private companion object {
        const val MAX_STREAMS = 1
        const val LOAD_PRIORITY = 1
        const val LOAD_STATUS_SUCCESS = 0
        const val VOLUME = 1f
        const val PLAY_PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f

        fun createSoundPool(): SoundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(soundEffectAttributes())
            .build()
    }
}

/** Атрибуты звуков интерфейса: sonification, глушатся беззвучным режимом. */
internal fun soundEffectAttributes(): AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()
