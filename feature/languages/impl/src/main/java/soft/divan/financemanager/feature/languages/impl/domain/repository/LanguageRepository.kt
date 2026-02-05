package soft.divan.financemanager.feature.languages.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.languages.impl.domain.model.Language

interface LanguageRepository {
    fun observeLanguage(): Flow<Language?>
    suspend fun setLanguage(language: Language)
}
