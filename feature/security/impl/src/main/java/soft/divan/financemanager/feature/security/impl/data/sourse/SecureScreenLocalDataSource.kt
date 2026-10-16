package soft.divan.financemanager.feature.security.impl.data.sourse

import kotlinx.coroutines.flow.Flow

/** Хранит, прятать ли содержимое экрана (`FLAG_SECURE`). */
interface SecureScreenLocalDataSource {

    /** Включена ли защита экрана; по умолчанию — да. */
    fun observeEnabled(): Flow<Boolean>

    /** Включает или выключает защиту экрана. */
    suspend fun setEnabled(enabled: Boolean)
}
