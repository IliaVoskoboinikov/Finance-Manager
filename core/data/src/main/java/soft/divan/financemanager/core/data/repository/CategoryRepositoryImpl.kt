package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.data.util.safeDbFlow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val localDataSource: CategoryLocalDataSource,
    private val syncManager: CategorySyncManager,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val errorLogger: ErrorLogger
) : CategoryRepository {

    override fun getAll(): Flow<DomainResult<List<Category>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            syncManager.pullServerData()
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getAll().map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getByType(isIncome: Boolean): Flow<DomainResult<List<Category>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
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