package soft.divan.financemanager.feature.sounds.impl.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.sounds.impl.data.source.SoundsLocalSource
import soft.divan.financemanager.feature.sounds.impl.domain.repository.SoundsRepository
import javax.inject.Inject

class SoundsRepositoryImpl @Inject constructor(
    private val soundsLocalSource: SoundsLocalSource
) : SoundsRepository {

    override fun observeSoundsEnabled(): Flow<Boolean> {
        return soundsLocalSource.getSoundEnabled()
    }

    override suspend fun setSoundsEnabled(isEnabled: Boolean) {
        soundsLocalSource.setSoundEnabled(isEnabled)
    }
}
// Revue me>>
