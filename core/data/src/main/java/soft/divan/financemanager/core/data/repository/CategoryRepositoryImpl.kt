package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
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

    override fun getByType(isIncome: Boolean): Flow<DomainResult<List<Category>>> {
        appCoroutineContext.launch {
            safeApiCall(errorLogger) {
                remoteDataSource.getByType(isIncome)
            }.onSuccess { categoryDtos ->
                safeDbCall(errorLogger) {
                    localDataSource.insert(categoryDtos.map { it.toEntity() })
                }
            }
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getByType(isIncome).map { list -> list.map { it.toDomain() } }
        }
    }
}
