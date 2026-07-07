package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.TransactionRollbackException
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

/**
 * Фейковый [TransactionRunner], повторяющий семантику RoomTransactionRunner без Room:
 * выполняет блок и превращает [TransactionRollbackException] в [DomainResult.Failure].
 */
class FakeTransactionRunner : TransactionRunner {
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        try {
            block()
        } catch (e: TransactionRollbackException) {
            DomainResult.Failure(e.error) as T
        }
}

val unknownError: DomainError = DomainError.Unknown(null)

fun account(balance: String): Account = Account(
    id = "account-1",
    name = "Main",
    balance = BigDecimal(balance),
    currencyId = "currency-1",
    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-01-01T00:00:00Z")
)

fun transaction(amount: String, type: TransactionType): Transaction = Transaction(
    id = "transaction-1",
    accountLocalId = "account-1",
    targetAccountLocalId = null,
    currencyId = "currency-1",
    categoryId = "category-1",
    amount = BigDecimal(amount),
    type = type,
    transactionDate = Instant.parse("2026-06-01T12:00:00Z"),
    comment = null,
    createdAt = Instant.parse("2026-06-01T12:00:00Z"),
    updatedAt = Instant.parse("2026-06-01T12:00:00Z")
)
