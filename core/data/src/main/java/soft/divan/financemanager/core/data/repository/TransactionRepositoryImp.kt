package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImp @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler
) : TransactionRepository {

    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<Transaction>> {

        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = transactionRemoteDataSource.getTransactionsByAccountAndPeriod(
                accountId, startDate, endDate
            )
            val transactionsByAccountAndPeriodDto = response.body().orEmpty()
            val transactionsByAccountAndPeriodEntity =
                transactionsByAccountAndPeriodDto.map { it.toEntity() }
            transactionLocalDataSource.insertTransactions(transactionsByAccountAndPeriodEntity)
        }

        val transactionsByAccountAndPeriodFlow =
            transactionLocalDataSource.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)
                .map { list -> list.map { it.toDomain() } }

        return transactionsByAccountAndPeriodFlow
    }
}