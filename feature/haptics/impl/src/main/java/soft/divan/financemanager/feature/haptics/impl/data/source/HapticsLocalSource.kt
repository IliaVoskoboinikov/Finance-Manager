package soft.divan.financemanager.feature.haptics.impl.data.source

import kotlinx.coroutines.flow.Flow

interface HapticsLocalSource {
    fun getHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(isEnabled: Boolean)
}