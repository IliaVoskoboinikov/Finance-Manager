package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger

/** Тесты [OutboxRepositoryImpl] — того, что о состоянии очереди видит presentation-слой. */
class OutboxRepositoryImplTest {

    private val localDataSource = mockk<OutboxLocalDataSource>()
    private val processor = mockk<OutboxProcessor>(relaxed = true)
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val repository = OutboxRepositoryImpl(
        localDataSource = localDataSource,
        processor = processor,
        errorLogger = errorLogger
    )

    @Test
    fun `observeFailedCount emits the number of stuck operations`() = runTest {
        every { localDataSource.observeFailedCount() } returns flowOf(3)

        val result = repository.observeFailedCount().first()

        assertThat(result).isEqualTo(DomainResult.Success(3))
    }

    @Test
    fun `observeFailedCount emits Failure when the query fails`() = runTest {
        val boom = RuntimeException("query failed")
        every { localDataSource.observeFailedCount() } returns flow { throw boom }

        val result = repository.observeFailedCount().first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    @Test
    fun `retryFailed requeues the entries and processes them immediately`() = runTest {
        coEvery { localDataSource.requeueFailed(any()) } returns 2

        val result = repository.retryFailed()

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        // Пользователь нажал «повторить» — ждать следующего фонового прогона он не должен
        coVerify(exactly = 1) { localDataSource.requeueFailed(any()) }
        coVerify(exactly = 1) { processor.process() }
    }

    @Test
    fun `retryFailed returns Failure when requeue throws`() = runTest {
        val boom = IllegalStateException("db")
        coEvery { localDataSource.requeueFailed(any()) } throws boom

        val result = repository.retryFailed()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        coVerify(exactly = 0) { processor.process() }
    }
}
