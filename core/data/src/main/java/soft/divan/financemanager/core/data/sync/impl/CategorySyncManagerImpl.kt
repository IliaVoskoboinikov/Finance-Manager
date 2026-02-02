package soft.divan.financemanager.core.data.sync.impl

import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.util.Syncable
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import javax.inject.Inject

class CategorySyncManagerImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val localDataSource: CategoryLocalDataSource,
    private val errorLogger: ErrorLogger
) : CategorySyncManager, Syncable {

    /** Запуск синхронизации через workManager */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
        }.isSuccess
    }

    /** Получаем данные с сервера и обновляем локальную БД разрешая конфликты */
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