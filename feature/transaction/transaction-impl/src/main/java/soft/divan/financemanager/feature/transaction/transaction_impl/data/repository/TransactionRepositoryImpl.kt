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

        // Пытаемся сначала получить транзакцию из локальной БД
        val localTransaction = transactionLocalDataSource.getTransactionById(id)?.toDomain()
        if (localTransaction != null) {
            // Асинхронно обновляем данные с сервера
            applicationScope.launch(dispatcher + exceptionHandler) {
                val response = transactionRemoteDataSource.getTransaction(id)
                if (response.isSuccessful) {
                    val updatedTransaction = response.body()?.toEntity()
                    if (updatedTransaction != null) {
                        transactionLocalDataSource.updateTransaction(updatedTransaction)
                    }
                }
            }
            return Result.success(localTransaction)
        }


        // Если локально нет — запрашиваем с сервера
        val response = transactionRemoteDataSource.getTransaction(id)
        if (response.isSuccessful) {
            val transactionDto = response.body() ?: throw IllegalStateException("Response body is null")
            val transaction = transactionDto.toEntity().toDomain()

            // Сохраняем в локальную базу для кеширования
            transactionLocalDataSource.saveTransaction(transaction.toEntity())

            return Result.success(transaction)
        } else {
            throw Exception("Failed to fetch transaction: ${response.code()} ${response.message()}")
        }
    }

    //todo баг с измененим транзакции
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

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        transactionLocalDataSource.saveTransaction(transaction.toEntity())
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response =
                transactionRemoteDataSource.updateTransaction(
                    id = transaction.id,
                    transaction = transaction.toDto()
                )
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

    //todo
    override suspend fun deleteTransaction(transactionId: Int): Result<Unit> = runCatching {
        transactionLocalDataSource.deleteTransaction(transactionId)

        // Асинхронно пытаемся удалить на сервере
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = transactionRemoteDataSource.deleteTransaction(transactionId)
            if (!response.isSuccessful) {
                // Если серверное удаление не удалось — можно добавить обработку
                // Например, логирование или флаг "требует синхронизации"
                // syncManager.markForDeletion(transactionId)
            }
        }
    }


}