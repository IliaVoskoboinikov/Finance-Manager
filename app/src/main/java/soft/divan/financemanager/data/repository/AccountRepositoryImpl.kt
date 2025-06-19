package soft.divan.financemanager.data.repository

import jakarta.inject.Inject
import soft.divan.financemanager.data.network.mapper.AccountDataMapper
import soft.divan.financemanager.data.network.mapper.AccountDomainMapper
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.utils.Rezult
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


}
