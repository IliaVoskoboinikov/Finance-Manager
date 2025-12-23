package soft.divan.financemanager.feature.haptic.haptic_impl.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe.HapticLocalSource
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.repository.HapticRepository
import javax.inject.Inject

class HapticRepositoryImpl @Inject constructor(
    private val hapticLocalSource: HapticLocalSource
) : HapticRepository {

    override fun observeHapticEnabled(): Flow<Boolean> {
        return hapticLocalSource.getHapticEnabled()
    }

    override suspend fun setHapticEnabled(isEnabled: Boolean) {
        hapticLocalSource.setHapticEnabled(isEnabled)
    }
}