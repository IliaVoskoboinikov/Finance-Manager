package soft.divan.financemanager.feature.transaction.transaction_impl.data.repository

import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionLocalDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toDomain
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toDto
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource
) : TransactionRepository {

    //todo
    override suspend fun getTransactions(id: Int): Result<Transaction> = runCatching {
        val response = transactionRemoteDataSource.getTransaction(id)
        val transactionDto = response.body()
        val transaction = transactionDto?.toEntity()?.toDomain()
        return Result.success(transaction!!)
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Unit> = runCatching {
//todo
        val response = if (transaction.id == -1)
            transactionRemoteDataSource.createTransaction(transaction.toDto())
        else
            transactionRemoteDataSource.updateTransaction(transaction.id, transaction.toDto())

        val transactionDto = response.body()!!
        /*val transactionEntity = transactionDto.toEntity()
        val transaction = transactionEntity.toDomain()*/
        return Result.success(Unit)

    }


}