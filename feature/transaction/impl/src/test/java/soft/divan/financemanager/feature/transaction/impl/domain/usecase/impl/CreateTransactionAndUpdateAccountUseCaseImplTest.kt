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

class CreateTransactionAndUpdateAccountUseCaseImplTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val accountRepository = mockk<AccountRepository>()
    private val useCase = CreateTransactionAndUpdateAccountUseCaseImpl(
        transactionRunner = FakeTransactionRunner(),
        transactionRepository = transactionRepository,
        accountRepository = accountRepository
    )

    @Test
    fun `income increases the balance locally and creates the transaction`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.create(transaction) } returns DomainResult.Success(Unit)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("150")) }
        coVerify { transactionRepository.create(transaction) }
    }

    @Test
    fun `expense decreases the balance locally`() = runTest {
        val transaction = transaction(amount = "30", type = TransactionType.EXPENSE)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.create(transaction) } returns DomainResult.Success(Unit)

        useCase(transaction)

        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("70")) }
    }

    @Test
    fun `does not push the account to the server`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.create(transaction) } returns DomainResult.Success(Unit)

        useCase(transaction)

        // Сервер сам пересчитывает баланс при пуше транзакции — update() запрещён.
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `rolls back when account is not found`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Failure(unknownError)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify(exactly = 0) { transactionRepository.create(any()) }
    }

    @Test
    fun `rolls back when balance update fails`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Failure(unknownError)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { transactionRepository.create(any()) }
    }

    @Test
    fun `rolls back when transaction creation fails`() = runTest {
        val transaction = transaction(amount = "50", type = TransactionType.INCOME)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.create(transaction) } returns
            DomainResult.Failure(unknownError)

        val result = useCase(transaction)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
    }
}
