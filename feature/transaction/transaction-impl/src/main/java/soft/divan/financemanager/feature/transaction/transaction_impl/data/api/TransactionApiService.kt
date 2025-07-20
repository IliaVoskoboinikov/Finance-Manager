package soft.divan.financemanager.feature.transaction.transaction_impl.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto

interface TransactionApiService {

    @POST("v1/transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto
    ): Response<TransactionRequestDto>

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