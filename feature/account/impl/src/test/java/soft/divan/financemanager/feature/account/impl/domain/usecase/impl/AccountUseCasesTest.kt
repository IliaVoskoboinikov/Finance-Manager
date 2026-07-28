package soft.divan.financemanager.feature.account.impl.domain.usecase.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.Instant

class AccountUseCasesTest {

    private val repository = mockk<AccountRepository>()
    private val transactionRepository = mockk<TransactionRepository>()

    private val account = Account(
        id = "local-1",
        name = "Cash",
        balance = BigDecimal.TEN,
        currencyId = "rub-id",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test
    fun `CreateAccountUseCase delegates to repository`() = runTest {
        coEvery { repository.create(account) } returns DomainResult.Success(Unit)

        val result = CreateAccountUseCaseImpl(repository)(account)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify(exactly = 1) { repository.create(account) }
    }

    @Test
    fun `CreateAccountUseCase propagates failure`() = runTest {
        coEvery { repository.create(account) } returns
            DomainResult.Failure(DomainError.NetworkUnavailable)

        val result = CreateAccountUseCaseImpl(repository)(account)

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NetworkUnavailable))
    }

    @Test
    fun `UpdateAccountUseCase delegates to repository`() = runTest {
        coEvery { repository.update(account) } returns DomainResult.Success(Unit)

        val result = UpdateAccountUseCaseImpl(repository)(account)

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify(exactly = 1) { repository.update(account) }
    }

    @Test
    fun `GetAccountByIdUseCase delegates to repository`() = runTest {
        coEvery { repository.getById("local-1") } returns DomainResult.Success(account)

        val result = GetAccountByIdUseCaseImpl(repository)("local-1")

        assertThat(result).isEqualTo(DomainResult.Success(account))
    }

    @Test
    fun `GetAccountByIdUseCase propagates failure`() = runTest {
        coEvery { repository.getById("missing") } returns DomainResult.Failure(DomainError.NoData)

        val result = GetAccountByIdUseCaseImpl(repository)("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `DeleteAccountUseCase delegates to repository`() = runTest {
        coEvery { repository.delete("local-1") } returns DomainResult.Success(Unit)

        val result = DeleteAccountUseCaseImpl(repository)("local-1")

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify(exactly = 1) { repository.delete("local-1") }
    }

    @Test
    fun `HasAccountTransactionsUseCase delegates to transaction repository`() = runTest {
        coEvery { transactionRepository.hasTransactions("local-1") } returns
            DomainResult.Success(true)

        val result = HasAccountTransactionsUseCaseImpl(transactionRepository)("local-1")

        assertThat(result).isEqualTo(DomainResult.Success(true))
        coVerify(exactly = 1) { transactionRepository.hasTransactions("local-1") }
    }

    @Test
    fun `HasAccountTransactionsUseCase propagates failure`() = runTest {
        coEvery { transactionRepository.hasTransactions("local-1") } returns
            DomainResult.Failure(DomainError.Unknown(null))

        val result = HasAccountTransactionsUseCaseImpl(transactionRepository)("local-1")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(null)))
    }
}
