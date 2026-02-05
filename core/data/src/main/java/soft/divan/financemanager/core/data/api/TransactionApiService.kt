package soft.divan.financemanager.core.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto

interface TransactionApiService {

    @POST("v1/transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto
    ): Response<TransactionResponseCreateDto>

    @GET("v1/transactions/account/{accountId}/period")
    suspend fun getTransactionsByAccountAndPeriod(
        @Path("accountId") accountId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<TransactionDto>>

    @GET("v1/transactions/{id}")
    suspend fun getTransaction(
        @Path("id") id: Int
    ): Response<TransactionDto>

    @PUT("v1/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Int,
        @Body request: TransactionRequestDto
    ): Response<TransactionDto>

    @DELETE("v1/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: Int
    ): Response<Unit>
}
