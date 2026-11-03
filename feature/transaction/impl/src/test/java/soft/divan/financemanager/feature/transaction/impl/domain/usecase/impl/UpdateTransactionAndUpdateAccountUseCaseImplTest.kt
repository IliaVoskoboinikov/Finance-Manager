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
        // Счёт тот же — одна запись итогового баланса, без промежуточной
        coVerify(exactly = 1) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("80")) }
        coVerify { transactionRepository.update(updated) }
        // Сервер сам пересчитывает баланс при PUT /transaction — update(Account) запрещён.
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `moving to another account refunds the old one and charges the new one`() = runTest {
        // Расход 100 переезжает со счёта 1 (баланс 100) на счёт 2 (баланс 50):
        // счёт 1 получает сумму обратно → 200, со счёта 2 она списывается → −50.
        val oldTransaction = transaction(amount = "100", type = TransactionType.EXPENSE)
        val updated = transaction(
            amount = "100",
            type = TransactionType.EXPENSE,
            accountLocalId = "account-2"
        )
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.getById("account-2") } returns
            DomainResult.Success(account(balance = "50", id = "account-2"))
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)
        coEvery { transactionRepository.update(updated) } returns DomainResult.Success(Unit)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify(exactly = 1) { accountRepository.updateBalanceLocal("account-1", BigDecimal("200")) }
        coVerify(exactly = 1) { accountRepository.updateBalanceLocal("account-2", BigDecimal("-50")) }
        coVerify(exactly = 2) { accountRepository.updateBalanceLocal(any(), any()) }
        coVerify { transactionRepository.update(updated) }
    }

    @Test
    fun `moving with a new amount and type reverts the old one and applies the new one`() =
        runTest {
            // Расход 30 со счёта 1 (баланс 100) становится доходом 50 на счёте 2 (баланс 10):
            // счёт 1 получает 30 обратно → 130, счёт 2 получает 50 → 60.
            val oldTransaction = transaction(amount = "30", type = TransactionType.EXPENSE)
            val updated = transaction(
                amount = "50",
                type = TransactionType.INCOME,
                accountLocalId = "account-2"
            )
            coEvery { transactionRepository.getById("transaction-1") } returns
                DomainResult.Success(oldTransaction)
            coEvery { accountRepository.getById("account-1") } returns
                DomainResult.Success(account(balance = "100"))
            coEvery { accountRepository.getById("account-2") } returns
                DomainResult.Success(account(balance = "10", id = "account-2"))
            coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
                DomainResult.Success(Unit)
            coEvery { transactionRepository.update(updated) } returns DomainResult.Success(Unit)

            useCase(updated)

            coVerify { accountRepository.updateBalanceLocal("account-1", BigDecimal("130")) }
            coVerify { accountRepository.updateBalanceLocal("account-2", BigDecimal("60")) }
            coVerify(exactly = 2) { accountRepository.updateBalanceLocal(any(), any()) }
        }

    @Test
    fun `rolls back when the old account balance cannot be written`() = runTest {
        val oldTransaction = transaction(amount = "100", type = TransactionType.EXPENSE)
        val updated = transaction(
            amount = "100",
            type = TransactionType.EXPENSE,
            accountLocalId = "account-2"
        )
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.updateBalanceLocal("account-1", any()) } returns
            DomainResult.Failure(unknownError)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { accountRepository.getById("account-2") }
        coVerify(exactly = 0) { transactionRepository.update(any()) }
    }

    @Test
    fun `rolls back when the new account balance cannot be written`() = runTest {
        val oldTransaction = transaction(amount = "100", type = TransactionType.EXPENSE)
        val updated = transaction(
            amount = "100",
            type = TransactionType.EXPENSE,
            accountLocalId = "account-2"
        )
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.getById("account-2") } returns
            DomainResult.Success(account(balance = "50", id = "account-2"))
        coEvery { accountRepository.updateBalanceLocal("account-1", any()) } returns
            DomainResult.Success(Unit)
        coEvery { accountRepository.updateBalanceLocal("account-2", any()) } returns
            DomainResult.Failure(unknownError)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { transactionRepository.update(any()) }
    }

    @Test
    fun `rolls back when the new account is not found`() = runTest {
        val oldTransaction = transaction(amount = "100", type = TransactionType.EXPENSE)
        val updated = transaction(
            amount = "100",
            type = TransactionType.EXPENSE,
            accountLocalId = "account-2"
        )
        coEvery { transactionRepository.getById("transaction-1") } returns
            DomainResult.Success(oldTransaction)
        coEvery { accountRepository.getById("account-1") } returns
            DomainResult.Success(account(balance = "100"))
        coEvery { accountRepository.getById("account-2") } returns
            DomainResult.Failure(unknownError)
        coEvery { accountRepository.updateBalanceLocal(any(), any()) } returns
            DomainResult.Success(Unit)

        val result = useCase(updated)

        assertThat(result).isEqualTo(DomainResult.Failure(unknownError))
        coVerify(exactly = 0) { transactionRepository.update(any()) }
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
