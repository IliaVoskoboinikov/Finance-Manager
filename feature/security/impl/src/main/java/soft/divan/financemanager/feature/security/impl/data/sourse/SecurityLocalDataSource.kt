package soft.divan.financemanager.feature.security.impl.data.sourse

import kotlinx.coroutines.flow.Flow

interface SecurityLocalDataSource {
    fun savePin(pin: String)
    fun getPin(): String?
    fun isPinSet(): Boolean

    /** Задан ли PIN — с обновлениями: PIN создаётся на одном экране, а виден на другом. */
    fun observePinSet(): Flow<Boolean>
    fun deletePin()
}
