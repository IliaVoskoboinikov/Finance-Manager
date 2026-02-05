package soft.divan.financemanager.feature.languages.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.languages.impl.domain.model.Language

interface ObserveLanguagesUseCase {
    operator fun invoke(): Flow<Language>
}
