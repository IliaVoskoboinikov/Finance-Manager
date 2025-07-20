package soft.divan.financemanager.feature.transaction.transaction_impl.data.repository

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toDto
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource

) : TransactionRepository {

    override suspend fun getTransactions(id: String): Result<List<Transaction>> {
        TODO("Not yet implemented")
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Unit> {
        val response = transactionRemoteDataSource.createTransaction(transaction.toDto())
        val transactionDto = response.body()!!
        /*val transactionEntity = transactionDto.toEntity()
        val transaction = transactionEntity.toDomain()*/
        return Result.success(Unit)

    }


}