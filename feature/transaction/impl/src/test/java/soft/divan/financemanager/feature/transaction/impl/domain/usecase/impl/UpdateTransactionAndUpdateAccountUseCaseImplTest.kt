package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal

class UpdateTransactionAndUpdateAccountUseCaseImplTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val accountRepository = mockk<AccountRepository>()
    private val useCase = UpdateTransactionAndUpdateAccountUseCaseImpl(
        transactionRunner = FakeTransactionRunner(),
        transactionRepository = transactionRepository,
        accountRepository = accountRepository
    )

    @Test
    fun `reverts the old amount and applies the new one locally`() = runTest {
        // Старая: расход 30 (баланс 100 = old - 30) → откат даёт 130, новая: расход 50 → 80.
        val oldTransaction = transaction(amount = "30", type = TransactionType.EXPENSE)
        val updated = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.update(updated) } returns DomainResult.Success(Unit)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("80")) }
        coVerify { transactionRepository.update(updated) }
        // Сервер сам пересчитывает баланс при PUT /transaction — update(Account) запрещён.
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `rolls back when old transaction is not found`() = runTest {
        val updated = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify(exactly = 0) { transactionRepository.update(any()) }
    }

    @Test
    fun `rolls back when transaction update fails`() = runTest {
        val oldTransaction = transaction(amount = "30", type = TransactionType.EXPENSE)
        val updated = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.update(updated) } returns
            DomainResult.Failure(unknownError)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
    }
}
