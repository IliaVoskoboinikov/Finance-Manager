package soft.divan.financemanager.feature.languages.impl.precenter

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import soft.divan.financemanager.feature.languages.impl.domain.usecase.ObserveLanguagesUseCase
import soft.divan.financemanager.feature.languages.impl.domain.usecase.SetLanguageUseCase
import soft.divan.financemanager.feature.languages.impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.languages.impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.languages.impl.precenter.model.LanguageUi
import soft.divan.financemanager.feature.languages.impl.precenter.model.LanguageUiState
import soft.divan.financemanager.feature.languages.impl.precenter.viewModel.LanguagesViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagePresenterTest {

    /* ---------- mapper ---------- */

    @Test
    fun `presentation mapper is bidirectional`() {
        Language.entries.forEach { language ->
            assertThat(language.toUi().toDomain()).isEqualTo(language)
        }
        LanguageUi.entries.forEach { ui ->
            assertThat(ui.toDomain().toUi()).isEqualTo(ui)
        }
    }

    /* ---------- view model ---------- */

    private val setLanguageUseCase = mockk<SetLanguageUseCase>(relaxUnitFun = true)
    private val observeLanguagesUseCase = mockk<ObserveLanguagesUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LanguagesViewModel(
        setLanguageUseCase = setLanguageUseCase,
        observeLanguagesUseCase = observeLanguagesUseCase
    )

    @Test
    fun `state publishes current language`() = runTest {
        every { observeLanguagesUseCase() } returns flowOf(Language.RUSSIAN)

        val vm = viewModel()
        val state = vm.uiState.first { it !is LanguageUiState.Loading }

        assertThat(state).isEqualTo(LanguageUiState.Success(LanguageUi.RUSSIAN))
    }

    @Test
    fun `state publishes Error when observation fails`() = runTest {
        every { observeLanguagesUseCase() } returns
            flow { throw IllegalStateException("boom") }

        val vm = viewModel()
        val state = vm.uiState.first { it !is LanguageUiState.Loading }

        assertThat(state).isInstanceOf(LanguageUiState.Error::class.java)
    }

    @Test
    fun `onLanguageSelected delegates to use case with domain model`() = runTest {
        every { observeLanguagesUseCase() } returns flowOf(Language.ENGLISH)

        viewModel().onLanguageSelected(LanguageUi.RUSSIAN)

        coVerify(exactly = 1) { setLanguageUseCase(Language.RUSSIAN) }
    }
}
