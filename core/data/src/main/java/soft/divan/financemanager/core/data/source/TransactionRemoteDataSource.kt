package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto

/**
 * Мутирующие методы требуют `idempotencyKey` — идентификатор операции, стабильный на все её
 * повторы. Источник ключа один: запись очереди исходящих операций, поэтому мутация без ключа
 * невозможна по сигнатуре.
 */
interface TransactionRemoteDataSource {
    suspend fun create(
        request: TransactionRequestDto,
        idempotencyKey: String
    ): Response<TransactionDto>

    suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>

    suspend fun get(id: String): Response<TransactionDto>

    suspend fun update(
        id: String,
        transaction: UpdateTransactionRequestDto,
        idempotencyKey: String
    ): Response<Unit>

    suspend fun delete(id: String, idempotencyKey: String): Response<Unit>
}
