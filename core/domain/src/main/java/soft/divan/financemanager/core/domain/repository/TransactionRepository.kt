package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult
import java.time.Instant

interface TransactionRepository {
    suspend fun create(transaction: Transaction): DomainResult<Unit>
    fun getByAccountAndPeriod(
        accountId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<DomainResult<List<Transaction>>>

    suspend fun getById(localId: String): DomainResult<Transaction>
    suspend fun update(transaction: Transaction): DomainResult<Unit>
    suspend fun delete(id: String): DomainResult<Unit>

    /**
     * Есть ли у счёта хотя бы одна операция.
     *
     * Используется для выбора поведения при удалении счёта: если операции есть, счёт нельзя
     * удалить физически — он архивируется (см. `AccountRepository.delete`), поэтому UI по этому
     * флагу показывает соответствующее предупреждение.
     */
    suspend fun hasTransactions(accountId: String): DomainResult<Boolean>
}
