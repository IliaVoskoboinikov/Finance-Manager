package soft.divan.financemanager.feature.sounds.sounds_impl.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.sounds.sounds_impl.data.sourсe.SoundsLocalSource
import soft.divan.financemanager.feature.sounds.sounds_impl.domain.repository.SoundsRepository
import javax.inject.Inject

class SoundsRepositoryImpl @Inject constructor(
    private val soundLocalSource: SoundsLocalSource
) : SoundsRepository {

    override fun observeSoundsEnabled(): Flow<Boolean> {
        return soundLocalSource.getSoundEnabled()
    }

    override suspend fun setSoundsEnabled(isEnabled: Boolean) {
        soundLocalSource.setSoundEnabled(isEnabled)
    }
}