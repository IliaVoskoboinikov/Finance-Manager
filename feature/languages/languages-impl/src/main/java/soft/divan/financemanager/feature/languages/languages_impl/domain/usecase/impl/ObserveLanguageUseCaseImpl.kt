package soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.languages.languages_impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language
import soft.divan.financemanager.feature.languages.languages_impl.domain.repository.LanguageRepository
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.ObserveLanguagesUseCase
import javax.inject.Inject

class ObserveLanguageUseCaseImpl @Inject constructor(
    private val repository: LanguageRepository,
    private val appLocaleManager: AppLocaleManager
) : ObserveLanguagesUseCase {
    override operator fun invoke(): Flow<Language> = repository.observeLanguage()
        .map { language ->
            if (language != null) {
                language
            } else {
                val systemLanguage = appLocaleManager.getCurrent()
                repository.setLanguage(systemLanguage)
                systemLanguage
            }
        }
}
