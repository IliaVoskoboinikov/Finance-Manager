package soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe

import kotlinx.coroutines.flow.Flow

interface HapticLocalSource {
    fun getHapticEnabled(): Flow<Boolean>
    suspend fun setHapticEnabled(isEnabled: Boolean)
}