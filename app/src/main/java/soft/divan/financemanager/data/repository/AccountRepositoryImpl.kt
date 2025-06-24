package soft.divan.financemanager.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.data.network.mapper.AccountDataMapper
import soft.divan.financemanager.data.network.mapper.AccountDomainMapper
import soft.divan.financemanager.data.network.mapper.toAccountBriefDomain
import soft.divan.financemanager.data.network.mapper.toDomain
import soft.divan.financemanager.data.network.mapper.toEntity
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountDataMapper: AccountDataMapper,
    private val accountDomainMapper: AccountDomainMapper,
) : AccountRepository {

    override fun getAccounts(): Flow<List<Account>> = flow {
        val response = accountRemoteDataSource.getAccounts()
        val accountDto = response.body().orEmpty()
        val accountsEntity = accountDto.map { it.toEntity() }
        val accounts = accountsEntity.map { it.toDomain() }
        emit(accounts)

    }.flowOn(Dispatchers.IO)

    override fun createAccount(createAccountRequest: CreateAccountRequest): Flow<Account> = flow {
        val requestDto = CreateAccountRequestDto(
            name = createAccountRequest.name,
            balance = createAccountRequest.balance.toPlainString(),
            currency = createAccountRequest.currency
        )
        val response = accountRemoteDataSource.createAccount(requestDto)
        val accountDto = response.body()!!
        val accountsEntity = accountDto.toEntity()
        val accounts = accountsEntity.toDomain()
        emit(accounts)


    }

    override  fun updateAccount(accountBrief: AccountBrief): Flow<AccountBrief> = flow {
        val response = accountRemoteDataSource.updateAccount(accountBrief)
        val accountWithStatsDto = response.body()!!
        val accountBrief = accountWithStatsDto.toAccountBriefDomain()
        emit(accountBrief)
    }

}

