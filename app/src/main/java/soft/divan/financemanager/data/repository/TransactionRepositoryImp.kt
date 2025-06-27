package soft.divan.financemanager.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.data.network.dto.TransactionDto
import soft.divan.financemanager.data.network.mapper.toDomain
import soft.divan.financemanager.data.network.mapper.toEntity
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImp @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource

    ) : TransactionRepository{
    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<Transaction>> = flow {
        val response = transactionRemoteDataSource.getTransactionsByAccountAndPeriod(
            accountId,startDate,endDate
        )
        val transactionsByAccountAndPeriodDto = response.body().orEmpty()
        val transactionsByAccountAndPeriodEntity = transactionsByAccountAndPeriodDto.map { it.toEntity() }
        val transactionsByAccountAndPeriod = transactionsByAccountAndPeriodEntity.map { it.toDomain() }
        emit(transactionsByAccountAndPeriod)

    }
}