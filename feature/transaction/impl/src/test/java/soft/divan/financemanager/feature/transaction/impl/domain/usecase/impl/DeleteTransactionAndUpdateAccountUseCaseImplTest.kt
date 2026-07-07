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

class DeleteTransactionAndUpdateAccountUseCaseImplTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val accountRepository = mockk<AccountRepository>()
    private val useCase = DeleteTransactionAndUpdateAccountUseCaseImpl(
        transactionRunner = FakeTransactionRunner(),
        transactionRepository = transactionRepository,
        accountRepository = accountRepository
    )

    @Test
    fun `reverts the amount locally and deletes the transaction`() = runTest {
        // Удаляем расход 50 при балансе 100 → откат возвращает 150.
        val transaction = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Success(Unit)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("150")) }
        coVerify { transactionRepository.delete("transaction-1") }
        // Сервер сам откатывает баланс при DELETE /transaction — update(Account) запрещён.
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `reverting an income decreases the balance`() = runTest {
        val transaction = transaction(amount = "40", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Success(Unit)

        useCase(transaction)

        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("60")) }
    }

    @Test
    fun `rolls back when transaction deletion fails`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
    }
}
