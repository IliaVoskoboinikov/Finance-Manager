package soft.divan.financemanager.core.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

class GetAccountsUseCaseImplTest {

    private val repository: AccountRepository = mockk()
    private val useCase = GetAccountsUseCaseImpl(repository)

    private fun account(id: String) = Account(
        id = id,
        name = "Account $id",
        balance = BigDecimal.TEN,
        currencyId = "rub-id",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test
    fun `invoke delegates to repository getAll and emits its values`() = runTest {
        val accounts = listOf(account("1"), account("2"))
        every { repository.getAll() } returns flowOf(DomainResult.Success(accounts))

        val emissions = useCase().toList()

        assertThat(emissions).containsExactly(DomainResult.Success(accounts))
        verify(exactly = 1) { repository.getAll() }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        every { repository.getAll() } returns flowOf(DomainResult.Failure(DomainError.Unauthorized))

        val emissions = useCase().toList()

        assertThat(emissions).containsExactly(DomainResult.Failure(DomainError.Unauthorized))
    }

    @Test
    fun `invoke returns the exact same flow instance from repository`() {
        val flow: Flow<DomainResult<List<Account>>> = flowOf()
        every { repository.getAll() } returns flow

        assertThat(useCase()).isSameAs(flow)
    }
}
