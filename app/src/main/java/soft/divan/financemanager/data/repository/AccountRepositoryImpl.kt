package soft.divan.financemanager.data.repository

import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.data.network.mapper.AccountDataMapper
import soft.divan.financemanager.data.network.mapper.AccountDomainMapper
import soft.divan.financemanager.data.network.mapper.toAccountBriefDomain
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


    override suspend fun getAccounts(): Rezult<List<Account>> {
        when (val result = accountRemoteDataSource.getAccounts()) {
            is Rezult.Error -> {
                return Rezult.Error(result.exception)
            }

            is Rezult.Success -> {
                val accountsEntity = result.data.map { accountDataMapper.toEntity(it) }
                val accounts = accountsEntity.map { accountDomainMapper.toDomain(it) }
                return Rezult.Success(accounts)
            }
        }
    }

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
