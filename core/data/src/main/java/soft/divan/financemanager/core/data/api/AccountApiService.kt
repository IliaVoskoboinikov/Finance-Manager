package soft.divan.financemanager.core.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto

interface AccountApiService {

    @POST("api/v1/account")
    suspend fun createAccount(
        @Body createAccountRequestDto: CreateAccountRequestDto
    ): Response<AccountDto>

    @GET("api/v1/account")
    suspend fun getAccounts(): Response<List<AccountDto>>

    @GET("api/v1/account/{id}")
    suspend fun getById(
        @Path("id") id: String
    ): Response<AccountDto>

    @PUT("api/v1/account/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body request: UpdateAccountRequestDto
    ): Response<Unit>

    @DELETE("api/v1/account/{id}")
    suspend fun delete(
        @Path("id") id: String
    ): Response<Unit>
}
