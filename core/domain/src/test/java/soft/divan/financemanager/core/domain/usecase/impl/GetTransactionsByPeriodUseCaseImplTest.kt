package soft.divan.financemanager.core.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Period
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

class GetTransactionsByPeriodUseCaseImplTest {

    private val accountRepository: AccountRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val currencyRepository: CurrencyRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private val useCase = GetTransactionsByPeriodUseCaseImpl(
        accountRepository = accountRepository,
        transactionRepository = transactionRepository,
        currencyRepository = currencyRepository,
        categoryRepository = categoryRepository
    )

    private val period = Period(
        startDate = Instant.parse("2024-01-01T00:00:00Z"),
        endDate = Instant.parse("2024-01-31T23:59:59Z")
    )

    private val incomeCategory = category(id = "1", name = "Salary", emoji = "💰", isIncome = true)
    private val expenseCategory = category(id = "2", name = "Food", emoji = "🍔", isIncome = false)
    private val categories = listOf(incomeCategory, expenseCategory)

    private fun category(id: String, name: String, emoji: String, isIncome: Boolean) = Category(
        id = id,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )

    private fun account(id: String) = Account(
        id = id,
        name = "Account $id",
        balance = BigDecimal.ZERO,
        currencyId = "rub-id",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    private fun transaction(
        id: String,
        accountId: String,
        categoryId: String,
        date: Instant
    ) = Transaction(
        id = id,
        accountLocalId = accountId,
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = categoryId,
        amount = BigDecimal.ONE,
        type = TransactionType.EXPENSE,
        transactionDate = date,
        comment = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    private fun stubBase(
        accounts: DomainResult<List<Account>> = DomainResult.Success(emptyList()),
        cats: DomainResult<List<Category>> = DomainResult.Success(categories),
        currency: CurrencySymbol = CurrencySymbol.RUB
    ) {
        every { accountRepository.getAll() } returns flowOf(accounts)
        every { categoryRepository.getAll() } returns flowOf(cats)
        every { currencyRepository.getSelectedCurrency() } returns flowOf(currency)
    }

    @Test
    fun `returns failure when accounts repository fails`() = runTest {
        stubBase(accounts = DomainResult.Failure(DomainError.NetworkUnavailable))

        val result = useCase(isIncome = false, period = period).first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NetworkUnavailable))
        verify(exactly = 0) { transactionRepository.getByAccountAndPeriod(any(), any(), any()) }
    }

    @Test
    fun `returns failure when categories repository fails`() = runTest {
        stubBase(
            accounts = DomainResult.Success(listOf(account("1"))),
            cats = DomainResult.Failure(DomainError.NoData)
        )

        val result = useCase(isIncome = false, period = period).first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `accounts failure takes precedence over categories failure`() = runTest {
        stubBase(
            accounts = DomainResult.Failure(DomainError.Unauthorized),
            cats = DomainResult.Failure(DomainError.NoData)
        )

        val result = useCase(isIncome = false, period = period).first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unauthorized))
    }

    @Test
    fun `returns empty success when there are no accounts`() = runTest {
        stubBase(accounts = DomainResult.Success(emptyList()))

        val result = useCase(isIncome = false, period = period).first()

        assertThat(result).isEqualTo(
            DomainResult.Success(Triple(emptyList<Transaction>(), CurrencySymbol.RUB, categories))
        )
        verify(exactly = 0) { transactionRepository.getByAccountAndPeriod(any(), any(), any()) }
    }

    @Test
    fun `filters expense transactions and sorts by date descending`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"))))

        val older = transaction("t1", "1", expenseCategory.id, Instant.parse("2024-01-05T10:00:00Z"))
        val newer = transaction("t2", "1", expenseCategory.id, Instant.parse("2024-01-20T10:00:00Z"))
        val income = transaction("t3", "1", incomeCategory.id, Instant.parse("2024-01-15T10:00:00Z"))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(listOf(older, newer, income)))

        val result = useCase(isIncome = false, period = period).first()

        val success = result as DomainResult.Success
        assertThat(success.data.first).containsExactly(newer, older)
        assertThat(success.data.second).isEqualTo(CurrencySymbol.RUB)
        assertThat(success.data.third).isEqualTo(categories)
    }

    @Test
    fun `filters income transactions only`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"))))

        val income = transaction("t1", "1", incomeCategory.id, Instant.parse("2024-01-10T10:00:00Z"))
        val expense = transaction("t2", "1", expenseCategory.id, Instant.parse("2024-01-11T10:00:00Z"))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(listOf(income, expense)))

        val result = useCase(isIncome = true, period = period).first()

        val success = result as DomainResult.Success
        assertThat(success.data.first).containsExactly(income)
    }

    @Test
    fun `aggregates transactions from multiple accounts`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"), account("2"))))

        val t1 = transaction("t1", "1", expenseCategory.id, Instant.parse("2024-01-05T10:00:00Z"))
        val t2 = transaction("t2", "2", expenseCategory.id, Instant.parse("2024-01-25T10:00:00Z"))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(listOf(t1)))
        every {
            transactionRepository.getByAccountAndPeriod("2", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(listOf(t2)))

        val result = useCase(isIncome = false, period = period).first()

        val success = result as DomainResult.Success
        assertThat(success.data.first).containsExactly(t2, t1)
    }

    @Test
    fun `returns failure when any account transactions fail`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"), account("2"))))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(emptyList()))
        every {
            transactionRepository.getByAccountAndPeriod("2", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Failure(DomainError.Unknown(null)))

        val result = useCase(isIncome = false, period = period).first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(null)))
    }

    @Test
    fun `excludes transactions whose category is unknown`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"))))

        val unknownCategory =
            transaction("t1", "1", categoryId = "999", Instant.parse("2024-01-10T10:00:00Z"))
        val expense =
            transaction("t2", "1", expenseCategory.id, Instant.parse("2024-01-11T10:00:00Z"))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(listOf(unknownCategory, expense)))

        val result = useCase(isIncome = false, period = period).first()

        val success = result as DomainResult.Success
        assertThat(success.data.first).containsExactly(expense)
    }

    @Test
    fun `returns empty transaction list when account has no matching transactions`() = runTest {
        stubBase(accounts = DomainResult.Success(listOf(account("1"))))

        every {
            transactionRepository.getByAccountAndPeriod("1", period.startDate, period.endDate)
        } returns flowOf(DomainResult.Success(emptyList()))

        val result = useCase(isIncome = false, period = period).first()

        val success = result as DomainResult.Success
        assertThat(success.data.first).isEmpty()
        assertThat(success.data.second).isEqualTo(CurrencySymbol.RUB)
        assertThat(success.data.third).isEqualTo(categories)
    }
}
