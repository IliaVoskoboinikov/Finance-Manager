package soft.divan.financemanager.feature.sounds.impl.data.source

import kotlinx.coroutines.flow.Flow

interface SoundsLocalSource {
    fun getSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(isEnabled: Boolean)
}
