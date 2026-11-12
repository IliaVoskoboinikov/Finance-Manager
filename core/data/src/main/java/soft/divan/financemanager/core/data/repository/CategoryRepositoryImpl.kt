package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.database.holder.DataGenerationGuard
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val localDataSource: CategoryLocalDataSource,
    private val syncManager: CategorySyncManager,
    private val appCoroutineContext: AppCoroutineContext,
    private val dataGuard: DataGenerationGuard,
    private val errorLogger: ErrorLogger
) : CategoryRepository {

    override fun getAll(): Flow<DomainResult<List<Category>>> {
        appCoroutineContext.launch {
            syncManager.pullServerData()
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getAll().map { list -> list.map { it.toDomain() } }
        }
    }

    /**
     * Категории типа из базы и фоновое обновление с сервера. Обновление привязано к нынешним
     * данным ([DataGenerationGuard]): ответ, пришедший после выхода с очисткой, в новую базу не
     * пишется.
     */
    override fun getByType(isIncome: Boolean): Flow<DomainResult<List<Category>>> {
        appCoroutineContext.launch {
            dataGuard.bindToCurrentData {
                safeApiCall(errorLogger) {
                    remoteDataSource.getByType(isIncome)
                }.onSuccess { categoryDtos ->
                    safeDbCall(errorLogger) {
                        localDataSource.insert(categoryDtos.map { it.toEntity() })
                    }
                }
            }
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getByType(isIncome).map { list -> list.map { it.toDomain() } }
        }
    }
}
