package soft.divan.financemanager.feature.languages.impl.domain.usecase

import soft.divan.financemanager.feature.languages.impl.domain.model.Language

interface SetLanguageUseCase {
    suspend operator fun invoke(language: Language)
}