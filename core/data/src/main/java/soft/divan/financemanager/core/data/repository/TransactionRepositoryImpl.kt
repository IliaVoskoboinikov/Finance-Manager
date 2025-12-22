package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler
) : TransactionRepository {

    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<Transaction>> {

        applicationScope.launch(dispatcher + exceptionHandler) {
            val serverId = accountLocalDataSource.getAccountByLocalId(accountId)
            val response = transactionRemoteDataSource.getTransactionsByAccountAndPeriod(
                serverId?.serverId!!, startDate, endDate
            )
            val transactionsByAccountAndPeriodDto = response.body().orEmpty()
            val transactionsByAccountAndPeriodEntity =
                transactionsByAccountAndPeriodDto.map { it.toEntity(accountIdLocal = serverId.localId) }
            transactionLocalDataSource.insertTransactions(transactionsByAccountAndPeriodEntity)
        }

        val transactionsByAccountAndPeriodFlow =
            transactionLocalDataSource.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)
                .map { list -> list.map { it.toDomain() } }

        return transactionsByAccountAndPeriodFlow
    }

    //todo
    override suspend fun getTransaction(id: Int): Result<Transaction> = runCatching {

        // Пытаемся сначала получить транзакцию из локальной БД
        val localTransaction = transactionLocalDataSource.getTransactionById(id)?.toDomain()
        if (localTransaction != null) {
            // Асинхронно обновляем данные с сервера
            applicationScope.launch(dispatcher + exceptionHandler) {
                val response = transactionRemoteDataSource.getTransaction(id)
                if (response.isSuccessful) {
                    val updatedTransaction =
                        response.body()?.toEntity(accountIdLocal = localTransaction.accountId)
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
            val transaction =
                transactionDto.toEntity(accountIdLocal = UUID.randomUUID().toString()).toDomain()

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
            val accountEntity = accountLocalDataSource.getAccountByLocalId(transaction.accountId)!!
            val response =
                transactionRemoteDataSource.createTransaction(transaction.toDto(accountIdServer = accountEntity.serverId!!))
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
            val account = accountLocalDataSource.getAccountByLocalId(transaction.accountId)!!
            val response =
                transactionRemoteDataSource.updateTransaction(
                    id = transaction.idServer,
                    transaction = transaction.toDto(accountIdServer = account.serverId!!)
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