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
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(transaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Success(Unit)

        val result = useCase("transaction-1")

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("150")) }
        coVerify { transactionRepository.delete("transaction-1") }
        // Сервер сам откатывает баланс при DELETE /transaction — update(Account) запрещён.
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `rolls back when the stored transaction is not found`() = runTest {
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase("transaction-1")

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify(exactly = 0) { transactionRepository.delete(any()) }
    }

    @Test
    fun `rolls back when the account of the stored transaction is not found`() = runTest {
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(transaction(amount = "50", type = TransactionType.EXPENSE))
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase("transaction-1")

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify(exactly = 0) { transactionRepository.delete(any()) }
    }

    @Test
    fun `does not delete when the balance revert fails`() = runTest {
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(transaction(amount = "50", type = TransactionType.EXPENSE))
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Failure(unknownError)

        val result = useCase("transaction-1")

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { transactionRepository.delete(any()) }
    }

    @Test
    fun `reverting an income decreases the balance`() = runTest {
        val transaction = transaction(amount = "40", type = TransactionType.INCOME)
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(transaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Success(Unit)

        useCase("transaction-1")

        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("60")) }
    }

    @Test
    fun `rolls back when transaction deletion fails`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.EXPENSE)
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(transaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.delete("transaction-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase("transaction-1")

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
    }
}
