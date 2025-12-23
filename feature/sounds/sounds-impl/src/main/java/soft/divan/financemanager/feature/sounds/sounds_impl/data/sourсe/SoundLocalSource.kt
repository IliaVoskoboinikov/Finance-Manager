package soft.divan.financemanager.feature.sounds.sounds_impl.data.sourсe

import kotlinx.coroutines.flow.Flow

interface SoundsLocalSource {
    fun getSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(isEnabled: Boolean)
}