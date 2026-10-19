package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto

/**
 * Мутирующие методы требуют `idempotencyKey` — см. [TransactionRemoteDataSource].
 */
interface AccountRemoteDataSource {
    suspend fun create(
        request: CreateAccountRequestDto,
        idempotencyKey: String
    ): Response<AccountDto>

    suspend fun getAll(): Response<List<AccountDto>>

    suspend fun getById(id: String): Response<AccountDto>

    suspend fun update(
        id: String,
        account: UpdateAccountRequestDto,
        idempotencyKey: String
    ): Response<Unit>

    suspend fun delete(id: String, idempotencyKey: String): Response<Unit>
}
