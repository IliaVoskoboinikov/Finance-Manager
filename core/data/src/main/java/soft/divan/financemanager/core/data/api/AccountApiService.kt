package soft.divan.financemanager.core.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto

interface AccountApiService {

    @GET("v1/accounts")
    suspend fun getAccounts(): Response<List<AccountDto>>

    @GET("v1/accounts/{id}")
    suspend fun getById(
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
    ): Response<AccountDto>

    @DELETE("v1/accounts/{id}")
    suspend fun delete(
        @Path("id") id: Int,
    ): Response<Unit>

}