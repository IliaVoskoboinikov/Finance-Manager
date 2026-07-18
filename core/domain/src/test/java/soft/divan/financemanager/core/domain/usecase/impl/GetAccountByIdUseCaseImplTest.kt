package soft.divan.financemanager.core.domain.usecase.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

class GetAccountByIdUseCaseImplTest {

    private val repository: AccountRepository = mockk()
    private val useCase = GetAccountByIdUseCaseImpl(repository)

    private val account = Account(
        id = "a1",
        name = "Cash",
        balance = BigDecimal.TEN,
        currencyId = "rub-id",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test
    fun `invoke delegates to repository getById`() = runTest {
        coEvery { repository.getById("a1") } returns DomainResult.Success(account)

        val result = useCase("a1")

        assertThat(result).isEqualTo(DomainResult.Success(account))
        coVerify(exactly = 1) { repository.getById("a1") }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        coEvery { repository.getById("missing") } returns DomainResult.Failure(DomainError.NoData)

        val result = useCase("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }
}
