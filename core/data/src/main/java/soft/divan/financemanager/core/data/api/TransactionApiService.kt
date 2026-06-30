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
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto

interface TransactionApiService {

    @POST("api/v1/transaction")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto
    ): Response<TransactionDto>

    @GET("api/v1/transaction/{accountId}/period")
    suspend fun getTransactionsByAccountAndPeriod(
        @Path("accountId") accountId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<TransactionDto>>

    @GET("api/v1/transaction/{id}")
    suspend fun getTransaction(
        @Path("id") id: String
    ): Response<TransactionDto>

    @PUT("api/v1/transaction/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: UpdateTransactionRequestDto
    ): Response<Unit>

    @DELETE("api/v1/transaction/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): Response<Unit>
}
