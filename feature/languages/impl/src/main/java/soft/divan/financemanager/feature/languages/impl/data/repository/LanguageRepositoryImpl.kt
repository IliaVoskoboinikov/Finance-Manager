package soft.divan.financemanager.feature.languages.impl.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.languages.impl.data.mapper.toDomain
import soft.divan.financemanager.feature.languages.impl.data.source.LanguagesLocalSource
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import soft.divan.financemanager.feature.languages.impl.domain.repository.LanguageRepository
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val languagesLocalSource: LanguagesLocalSource,
) : LanguageRepository {

    override fun observeLanguage(): Flow<Language?> {
        return languagesLocalSource.observe().map { it?.toDomain() }
    }

    override suspend fun setLanguage(language: Language) {
        languagesLocalSource.save(language.tag)
    }

}