package soft.divan.financemanager.data.source

import soft.divan.financemanager.data.network.api.AccountApiService
import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.util.safeHttpResult
import soft.divan.financemanager.domain.utils.Rezult
import javax.inject.Inject

class AccountRemoteDataSourceImpl @Inject constructor(
    private val accountApiServiceApiService: AccountApiService,
) : AccountRemoteDataSource {
    override suspend fun getAccounts(): Rezult<List<AccountDto>> {
        return safeHttpResult { accountApiServiceApiService.getAccounts() }
    }
}