package soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.impl

import soft.divan.financemanager.feature.languages.languages_impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language
import soft.divan.financemanager.feature.languages.languages_impl.domain.repository.LanguageRepository
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.SetLanguageUseCase
import javax.inject.Inject

class SetLanguageUseCaseImpl @Inject constructor(
    private val repository: LanguageRepository,
    private val appLocaleManager: AppLocaleManager
) : SetLanguageUseCase {
    override suspend fun invoke(language: Language) {
        repository.setLanguage(language)
        appLocaleManager.apply(language)
    }
}