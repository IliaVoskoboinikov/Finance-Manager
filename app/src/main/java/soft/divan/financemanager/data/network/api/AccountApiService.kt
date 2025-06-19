package soft.divan.financemanager.data.network.api

import retrofit2.Response
import retrofit2.http.GET
import soft.divan.financemanager.data.network.dto.AccountDto

interface AccountApiService {

    @GET("v1/accounts")
    suspend fun getAccounts(): Response<List<AccountDto>>

}
