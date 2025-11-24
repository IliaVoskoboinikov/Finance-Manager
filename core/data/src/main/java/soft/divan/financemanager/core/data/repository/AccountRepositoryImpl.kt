package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toAccountBriefDomain
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.model.CreateAccountRequest
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
    override fun createAccount(createAccountRequest: CreateAccountRequest): Result<Unit> = runCatching {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = accountRemoteDataSource.createAccount(createAccountRequest.toDto())
        }
        return Result.success(Unit)
    }

    //todo
    override fun updateAccount(accountBrief: AccountBrief): Flow<AccountBrief> = flow {
        val response = accountRemoteDataSource.updateAccount(accountBrief)
        val accountWithStatsDto = response.body()!!
        val accountBrief = accountWithStatsDto.toAccountBriefDomain()
        emit(accountBrief)
    }

}