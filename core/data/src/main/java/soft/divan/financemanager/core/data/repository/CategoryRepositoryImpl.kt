package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.Syncable
import soft.divan.financemanager.core.data.Synchronizer
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.data.util.safeDbFlow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryRemoteDataSource: CategoryRemoteDataSource,
    private val categoryLocalDataSource: CategoryLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val errorLogger: ErrorLogger
) : CategoryRepository, Syncable {

    override suspend fun getCategories(): Flow<DomainResult<List<Category>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            pullServerData()
        }
        return safeDbFlow(errorLogger) {
            categoryLocalDataSource.getCategories().map { list -> list.map { it.toDomain() } }
        }
    }

    override suspend fun getCategoriesByType(isIncome: Boolean): Flow<DomainResult<List<Category>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val resultApi =
                safeApiCall(errorLogger) { categoryRemoteDataSource.getCategoriesByType(isIncome) }
            if (resultApi is DomainResult.Success) {
                safeDbCall(errorLogger) { categoryLocalDataSource.insertCategories(resultApi.data.map { it.toEntity() }) }
            }
        }

        return safeDbFlow(errorLogger) {
            categoryLocalDataSource.getCategoriesByType(isIncome)
                .map { list -> list.map { it.toDomain() } }
        }
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
        }.isSuccess
    }

    private suspend fun pullServerData() {
        val resultApi = safeApiCall(errorLogger) { categoryRemoteDataSource.getCategories() }
        if (resultApi is DomainResult.Success) {
            safeDbCall(errorLogger) { categoryLocalDataSource.insertCategories(resultApi.data.map { it.toEntity() }) }
        }
    }
}