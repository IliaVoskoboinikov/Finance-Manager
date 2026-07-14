package soft.divan.financemanager.feature.languages.impl.domain.usecase.impl

import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.languages.impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import soft.divan.financemanager.feature.languages.impl.domain.repository.LanguageRepository

class LanguageUseCasesTest {

    private val repository = mockk<LanguageRepository>(relaxUnitFun = true)
    private val appLocaleManager = mockk<AppLocaleManager>(relaxUnitFun = true)

    @Test
    fun `observe returns stored language as is`() = runTest {
        every { repository.observeLanguage() } returns flowOf(Language.RUSSIAN)

        val useCase = ObserveLanguageUseCaseImpl(repository, appLocaleManager)

        assertThat(useCase().first()).isEqualTo(Language.RUSSIAN)
        coVerify(exactly = 0) { repository.setLanguage(any()) }
    }

    @Test
    fun `observe falls back to system language and persists it`() = runTest {
        every { repository.observeLanguage() } returns flowOf(null)
        every { appLocaleManager.getCurrent() } returns Language.ENGLISH

        val useCase = ObserveLanguageUseCaseImpl(repository, appLocaleManager)

        assertThat(useCase().first()).isEqualTo(Language.ENGLISH)
        coVerify(exactly = 1) { repository.setLanguage(Language.ENGLISH) }
    }

    @Test
    fun `set language stores it then applies locale`() = runTest {
        val useCase = SetLanguageUseCaseImpl(repository, appLocaleManager)

        useCase(Language.RUSSIAN)

        coVerifyOrder {
            repository.setLanguage(Language.RUSSIAN)
            appLocaleManager.apply(Language.RUSSIAN)
        }
    }
}
