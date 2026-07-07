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
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import java.time.Instant

class GetCategoriesUseCaseImplTest {

    private val repository: CategoryRepository = mockk()
    private val useCase = GetCategoriesUseCaseImpl(repository)

    @Test
    fun `invoke delegates to repository getAll and emits its values`() = runTest {
        val categories = listOf(
            Category(
                id = "1",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                name = "Food",
                emoji = "🍔",
                isIncome = false
            )
        )
        val expected: Flow<DomainResult<List<Category>>> =
            flowOf(DomainResult.Success(categories))
        every { repository.getAll() } returns expected

        val emissions = useCase().toList()

        assertThat(emissions).containsExactly(DomainResult.Success(categories))
        verify(exactly = 1) { repository.getAll() }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        every { repository.getAll() } returns
            flowOf(DomainResult.Failure(soft.divan.financemanager.core.domain.error.DomainError.NoData))

        val emissions = useCase().toList()

        assertThat(emissions).containsExactly(
            DomainResult.Failure(soft.divan.financemanager.core.domain.error.DomainError.NoData)
        )
    }

    @Test
    fun `invoke returns the exact same flow instance from repository`() {
        val flow: Flow<DomainResult<List<Category>>> = flowOf()
        every { repository.getAll() } returns flow

        assertThat(useCase()).isSameAs(flow)
    }
}
