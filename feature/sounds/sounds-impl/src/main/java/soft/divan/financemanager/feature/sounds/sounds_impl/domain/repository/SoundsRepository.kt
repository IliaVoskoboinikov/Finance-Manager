package soft.divan.financemanager.feature.sounds.sounds_impl.domain.repository

import kotlinx.coroutines.flow.Flow

interface SoundsRepository {
    fun observeSoundsEnabled(): Flow<Boolean>
    suspend fun setSoundsEnabled(isEnabled: Boolean)
}