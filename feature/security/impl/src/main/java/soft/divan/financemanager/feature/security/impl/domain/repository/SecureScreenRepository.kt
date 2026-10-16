package soft.divan.financemanager.feature.security.impl.domain.repository

import kotlinx.coroutines.flow.Flow

/** Прятать ли содержимое приложения от скриншотов и превью в списке недавних. */
interface SecureScreenRepository {

    /** Включена ли защита экрана. */
    fun observeEnabled(): Flow<Boolean>

    /** Включает или выключает защиту экрана. */
    suspend fun setEnabled(enabled: Boolean)
}
