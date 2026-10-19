package soft.divan.financemanager.feature.security.impl.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    fun savePin(pin: String)
    fun verifyPin(pin: String): Boolean
    fun isPinSet(): Boolean

    /** Задан ли PIN — с обновлениями. */
    fun observePinSet(): Flow<Boolean>
    fun deletePin()
}
