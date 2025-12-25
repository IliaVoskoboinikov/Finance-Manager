package soft.divan.financemanager.feature.languages.languages_impl.domain.usecase

import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language

interface SetLanguageUseCase {
    suspend operator fun invoke(language: Language)
}