package soft.divan.financemanager.feature.haptics.impl.domain.repository

import kotlinx.coroutines.flow.Flow

interface HapticsRepository {
    fun observeHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(isEnabled: Boolean)
}