package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.repository.AccountRepository
import javax.inject.Inject


class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler
) : AccountRepository {

    override suspend fun getAccounts(): Flow<List<Account>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = accountRemoteDataSource.getAccounts()
            val accountDto = response.body().orEmpty()
            val accountEntities = accountDto.map { it.toEntity() }
            accountLocalDataSource.insertAccounts(accountEntities)
        }
        val accountsFlow =
            accountLocalDataSource.getAccounts().map { list -> list.map { it.toDomain() } }
        return accountsFlow

    }

    //todo добавить бд
    override fun createAccount(account: AccountBrief): Result<Unit> = runCatching {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = accountRemoteDataSource.createAccount(account.toDto())
        }
        return Result.success(Unit)
    }

    //todo
    override suspend fun updateAccount(account: AccountBrief): Result<Unit> =
        runCatching {
            // accountLocalDataSource.updateAccount(account.toEntity())
            applicationScope.launch(dispatcher + exceptionHandler) {
                val response =
                    accountRemoteDataSource.updateAccount(
                        id = account.id,
                        account = UpdateAccountRequestDto(
                            name = account.name,
                            balance = account.balance.toString(),
                            currency = account.currency
                        ),
                    )
                if (response.isSuccessful) {
//todo time
                    val account = response.body()!!
                    accountLocalDataSource.updateAccount(account.toEntity())
                }
            }
            return Result.success(Unit)
    }

    override suspend fun deleteAccount(id: Int): Result<Unit> = runCatching {
        accountLocalDataSource.deleteAccount(id)
        // Асинхронно пытаемся удалить на сервере
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = accountRemoteDataSource.delete(id)
            if (!response.isSuccessful) {
                // Если серверное удаление не удалось — можно добавить обработку
                // Например, логирование или флаг "требует синхронизации"
                // syncManager.markForDeletion(transactionId)
            }
        }
    }

    override suspend fun getAccountById(id: Int): Result<Account> = runCatching {
        val localAccount = accountLocalDataSource.getAccount(id)?.toDomain()
        if (localAccount != null) {
            // Асинхронно обновляем данные с сервера
            applicationScope.launch(dispatcher + exceptionHandler) {
                val response = accountRemoteDataSource.getById(id)
                if (response.isSuccessful) {
                    val updatedAccount = response.body()?.toEntity()
                    if (updatedAccount != null) {
                        accountLocalDataSource.updateAccount(updatedAccount)
                    }
                }
            }
            return Result.success(localAccount)
        }
        // Если локально нет — запрашиваем с сервера
        val response = accountRemoteDataSource.getById(id)
        if (response.isSuccessful) {
            val accountDto = response.body() ?: throw IllegalStateException("Response body is null")
            val account = accountDto.toDomain()

            // Сохраняем в локальную базу для кеширования
            accountLocalDataSource.saveAccount(accountDto.toEntity())

            return Result.success(account)
        } else {
            throw Exception("Failed to fetch transaction: ${response.code()} ${response.message()}")
        }
    }

}