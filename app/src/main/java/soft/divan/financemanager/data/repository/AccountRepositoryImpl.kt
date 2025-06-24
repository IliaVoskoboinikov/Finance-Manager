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
import soft.divan.financemanager.domain.utils.Rezult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountDataMapper: AccountDataMapper,
    private val accountDomainMapper: AccountDomainMapper,
) : AccountRepository {

    override fun getAccounts(): Flow<List<Account>> =  flow {
        val response = accountRemoteDataSource.getAccounts()
        if (response.isSuccessful) {
            val accountDto = response.body().orEmpty()
            val accountsEntity = accountDto.map { it.toEntity() }
            val accounts = accountsEntity.map {  it.toDomain() }
            emit(accounts)
        } else {
            throw Exception("Failed to fetch accounts: ${response.code()} ${response.message()}")
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun createAccount(createAccountRequest: CreateAccountRequest): Rezult<Account> {
        val requestDto = CreateAccountRequestDto(
            name = createAccountRequest.name,
            balance = createAccountRequest.balance.toPlainString(),
            currency = createAccountRequest.currency
        )
        when (val result = accountRemoteDataSource.createAccount(requestDto)) {
            is Rezult.Error -> return Rezult.Error(result.exception)
            is Rezult.Success -> {
                val accountEntity = accountDataMapper.toEntity(result.data)
                val account = accountDomainMapper.toDomain(accountEntity)
                return Rezult.Success(account)
            }

        }
    }

    override suspend fun updateAccount(accountBrief: AccountBrief): Rezult<AccountBrief> {
        return when (val result = accountRemoteDataSource.updateAccount(accountBrief)) {
            is Rezult.Success -> Rezult.Success(result.data.toAccountBriefDomain())
            is Rezult.Error -> Rezult.Error(result.exception)
        }
    }
}
