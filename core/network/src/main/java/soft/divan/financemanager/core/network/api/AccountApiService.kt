package soft.divan.financemanager.core.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import soft.divan.financemanager.core.network.dto.AccountDto
import soft.divan.financemanager.core.network.dto.AccountWithStatsDto
import soft.divan.financemanager.core.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.network.dto.UpdateAccountRequestDto

interface AccountApiService {

    @GET("v1/accounts")
    suspend fun getAccounts(): Response<List<AccountDto>>

    @GET("v1/accounts/{id}")
    suspend fun getAccountById(
        @Path("id") id: Int
    ): Response<AccountWithStatsDto>

    @POST("v1/accounts")
    suspend fun createAccount(
        @Body createAccountRequestDto: CreateAccountRequestDto
    ): Response<AccountDto>

    @PUT("v1/accounts/{id}")
    suspend fun updateAccount(
        @Path("id") accountId: Int,
        @Body request: UpdateAccountRequestDto
    ): Response<AccountWithStatsDto>

}
