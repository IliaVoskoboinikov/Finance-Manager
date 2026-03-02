package soft.divan.financemanager.core.data.sync.impl

import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

/**
 * Реализация [CategorySyncManager].
 *
 * Отвечает за синхронизацию категорий между сервером и локальной БД.
 *
 * Архитектурные принципы:
 * - Offline-first: локальная БД является источником истины
 * - Pull-синхронизация с сервера
 * - Разрешение конфликтов: серверные данные перезаписывают локальные
 * - Все операции обёрнуты в safeApiCall / safeDbCall
 */
class CategorySyncManagerImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val localDataSource: CategoryLocalDataSource,
    private val errorLogger: ErrorLogger
) : CategorySyncManager {

    /**
     * Точка входа полной синхронизации категорий.
     *
     * В текущей реализации выполняется только pull-синхронизация.
     *
     * Возвращает true, если pull завершился без исключений.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
        }.isSuccess
    }

    /**
     * Загружает актуальные категории с сервера и обновляет локальную БД.
     *
     * Алгоритм:
     * 1. Получаем все категории с сервера
     * 2. Мапим их в сущности локальной БД
     * 3. Вставляем или обновляем все записи через insert()
     *
     * Особенности:
     * - Используется last-write-wins (сервер перезаписывает локальные данные)
     * - Все операции обёрнуты в safeApiCall / safeDbCall
     */
    override suspend fun pullServerData() {
        safeApiCall(errorLogger) {
            remoteDataSource.getAll()
        }.onSuccess { categoryDtos ->
            safeDbCall(errorLogger) {
                localDataSource.insert(categoryDtos.map { it.toEntity() })
            }
        }
    }
}
