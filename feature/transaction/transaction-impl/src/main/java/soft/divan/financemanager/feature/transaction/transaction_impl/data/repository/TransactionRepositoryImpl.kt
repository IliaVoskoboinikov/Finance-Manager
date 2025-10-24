package soft.divan.financemanager.feature.transaction.transaction_impl.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionLocalDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toDomain
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toDto
import soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper.toEntity
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler
) : TransactionRepository {

    //todo
    override suspend fun getTransactions(id: Int): Result<Transaction> = runCatching {
        val response = transactionRemoteDataSource.getTransaction(id)
        val transactionDto = response.body()
        val transaction = transactionDto?.toEntity()?.toDomain()
        return Result.success(transaction!!)
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Unit> = runCatching {

        transactionLocalDataSource.saveTransaction(transaction.toEntity())

        applicationScope.launch(dispatcher + exceptionHandler) {
            val response =
                transactionRemoteDataSource.createTransaction(transaction.toDto())
            if (response.isSuccessful) {
                val serverTransaction = response.body()!!
                transactionLocalDataSource.updateTransactionId(
                    //todo время унифицировать во все прилке
                    createdAt = transaction.createdAt.atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    newId = serverTransaction.id
                )
            }
        }
        return Result.success(Unit)
    }

    //todo бд
    override suspend fun deleteTransaction(transactionId: Int): Result<Unit> = runCatching {
        val response = transactionRemoteDataSource.deleteTransaction(transactionId)
    }


}