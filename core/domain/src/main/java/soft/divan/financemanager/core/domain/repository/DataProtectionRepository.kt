package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult

/**
 * Уровень защиты ключа локальных данных и PIN, которым он завёрнут.
 *
 * Сама база при смене уровня не перешифровывается: меняется лишь то, чем закрыт её ключ.
 */
interface DataProtectionRepository {

    /** Текущий уровень защиты; молчит, пока ключи не созданы. */
    fun observeLevel(): Flow<DataProtectionLevel>

    /** Уровень защиты сейчас; `null` — ключей нет или их не прочитать. */
    suspend fun currentLevel(): DataProtectionLevel?

    /**
     * Меняет уровень защиты. [pin] обязателен, если текущий или новый уровень —
     * [DataProtectionLevel.PIN]. Перед включением PIN-уровня неотправленные изменения
     * залогиненного пользователя досылаются на сервер.
     */
    suspend fun changeLevel(target: DataProtectionLevel, pin: String?): ProtectionChangeResult

    /**
     * Меняет PIN, которым завёрнут ключ базы. На уровнях без PIN ключ от PIN не зависит — тогда
     * ничего не делает и возвращает [ProtectionChangeResult.Changed].
     */
    suspend fun changePin(currentPin: String, newPin: String): ProtectionChangeResult
}
