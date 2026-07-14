package soft.divan.financemanager.feature.category.impl.presenter.viewmodel

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.feature.category.impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.impl.presenter.model.CategoriesUiState
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val getCategoriesUseCase = mockk<GetCategoriesUseCase>()
    private val searchCategoryUseCase = mockk<SearchCategoryUseCase>()

    private fun category(id: String, name: String) = Category(
        id = id,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = name,
        emoji = "🍔",
        isIncome = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = CategoriesViewModel(
        getCategoriesUseCase,
        searchCategoryUseCase,
        UnconfinedTestDispatcher()
    )

    @Test
    fun `loads categories into Success state`() = runTest {
        every { getCategoriesUseCase() } returns flowOf(
            DomainResult.Success(listOf(category("1", "Food"), category("2", "Taxi")))
        )

        val vm = viewModel()
        val state = vm.uiState.first { it !is CategoriesUiState.Loading }

        val success = state as CategoriesUiState.Success
        assertThat(success.categories.map { it.name }).containsExactly("Food", "Taxi")
        assertThat(success.filteredCategories).isEqualTo(success.categories)
    }

    @Test
    fun `empty category list produces EmptyData`() = runTest {
        every { getCategoriesUseCase() } returns flowOf(DomainResult.Success(emptyList()))

        val vm = viewModel()
        val state = vm.uiState.first { it !is CategoriesUiState.Loading }

        assertThat(state).isEqualTo(CategoriesUiState.EmptyData)
    }

    @Test
    fun `failure produces Error state`() = runTest {
        every { getCategoriesUseCase() } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))

        val vm = viewModel()
        val state = vm.uiState.first { it !is CategoriesUiState.Loading }

        assertThat(state).isInstanceOf(CategoriesUiState.Error::class.java)
    }

    @Test
    fun `NoData failure also ends in Error state`() = runTest {
        // Текущее поведение: ветка NoData сначала ставит EmptyData,
        // но затем безусловно перезаписывается на Error
        every { getCategoriesUseCase() } returns
            flowOf(DomainResult.Failure(DomainError.NoData))

        val vm = viewModel()
        val state = vm.uiState.first { it !is CategoriesUiState.Loading }

        assertThat(state).isInstanceOf(CategoriesUiState.Error::class.java)
    }

    @Test
    fun `search filters categories keeping full list`() = runTest {
        every { getCategoriesUseCase() } returns flowOf(
            DomainResult.Success(listOf(category("1", "Food"), category("2", "Taxi")))
        )
        coEvery { searchCategoryUseCase("Fo", any()) } answers {
            secondArg<List<Category>>().filter { it.name.contains("Fo", ignoreCase = true) }
        }

        val vm = viewModel()
        vm.uiState.first { it !is CategoriesUiState.Loading }

        vm.search("Fo")

        val success = vm.uiState.first { state ->
            state is CategoriesUiState.Success && state.filteredCategories.size == 1
        } as CategoriesUiState.Success
        assertThat(success.filteredCategories.map { it.name }).containsExactly("Food")
        assertThat(success.categories.map { it.name }).containsExactly("Food", "Taxi")
    }

    @Test
    fun `search is ignored while not in Success state`() = runTest {
        every { getCategoriesUseCase() } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))

        val vm = viewModel()
        vm.uiState.first { it !is CategoriesUiState.Loading }

        vm.search("query")

        assertThat(vm.uiState.value).isInstanceOf(CategoriesUiState.Error::class.java)
    }
}
