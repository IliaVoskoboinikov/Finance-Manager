package soft.divan.financemanager.feature.languages.languages_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language

interface ObserveLanguagesUseCase {
    operator fun invoke(): Flow<Language>
}