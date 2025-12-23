package soft.divan.financemanager.feature.haptic.haptic_impl.domain.repository

import kotlinx.coroutines.flow.Flow

interface HapticRepository {
    fun observeHapticEnabled(): Flow<Boolean>
    suspend fun setHapticEnabled(isEnabled: Boolean)
}