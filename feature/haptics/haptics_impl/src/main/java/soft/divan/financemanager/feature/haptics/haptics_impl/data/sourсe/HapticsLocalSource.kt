package soft.divan.financemanager.feature.haptics.haptics_impl.data.sourсe

import kotlinx.coroutines.flow.Flow

interface HapticsLocalSource {
    fun getHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(isEnabled: Boolean)
}