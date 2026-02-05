package soft.divan.financemanager.feature.haptics.impl.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.haptics.impl.data.source.HapticsLocalSource
import soft.divan.financemanager.feature.haptics.impl.domain.repository.HapticsRepository
import javax.inject.Inject

class HapticsRepositoryImpl @Inject constructor(
    private val hapticsLocalSource: HapticsLocalSource
) : HapticsRepository {

    override fun observeHapticsEnabled(): Flow<Boolean> {
        return hapticsLocalSource.getHapticsEnabled()
    }

    override suspend fun setHapticsEnabled(isEnabled: Boolean) {
        hapticsLocalSource.setHapticsEnabled(isEnabled)
    }
}
