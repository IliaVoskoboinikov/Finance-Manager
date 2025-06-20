package soft.divan.financemanager.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto

interface AccountApiService {

    @GET("v1/accounts")
    suspend fun getAccounts(): Response<List<AccountDto>>

    @POST("v1/accounts")
    suspend fun createAccount(
        @Body createAccountRequestDto: CreateAccountRequestDto
    ): Response<AccountDto>
}
