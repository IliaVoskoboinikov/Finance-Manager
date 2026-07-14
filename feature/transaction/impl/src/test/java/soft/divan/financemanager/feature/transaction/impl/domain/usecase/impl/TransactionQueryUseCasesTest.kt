package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

class TransactionQueryUseCasesTest {

    private val categoryRepository = mockk<CategoryRepository>()
    private val transactionRepository = mockk<TransactionRepository>()

    @Test
    fun `GetCategoriesByTypeUseCase delegates to repository`() = runTest {
        val categories = listOf(
            Category(
                id = "1",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                name = "Salary",
                emoji = "💰",
                isIncome = true
            )
        )
        every { categoryRepository.getByType(true) } returns
            flowOf(DomainResult.Success(categories))

        val result = GetCategoriesByTypeUseCaseImpl(categoryRepository)(true).first()

        assertThat(result).isEqualTo(DomainResult.Success(categories))
    }

    @Test
    fun `GetTransactionUseCase delegates to repository`() = runTest {
        val transaction = Transaction(
            id = "t1",
            accountLocalId = "a1",
            targetAccountLocalId = null,
            currencyId = "rub-id",
            categoryId = "cat-1",
            amount = BigDecimal.TEN,
            type = TransactionType.EXPENSE,
            transactionDate = Instant.EPOCH,
            comment = null,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        coEvery { transactionRepository.getById("t1") } returns DomainResult.Success(transaction)

        val result = GetTransactionUseCaseImpl(transactionRepository)("t1")

        assertThat(result).isEqualTo(DomainResult.Success(transaction))
    }

    @Test
    fun `GetTransactionUseCase propagates failure`() = runTest {
        coEvery { transactionRepository.getById("missing") } returns
            DomainResult.Failure(DomainError.NoData)

        val result = GetTransactionUseCaseImpl(transactionRepository)("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }
}
