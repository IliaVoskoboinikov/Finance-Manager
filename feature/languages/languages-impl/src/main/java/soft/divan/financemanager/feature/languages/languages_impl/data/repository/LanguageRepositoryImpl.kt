package soft.divan.financemanager.feature.languages.languages_impl.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.languages.languages_impl.data.mapper.toDomain
import soft.divan.financemanager.feature.languages.languages_impl.data.sourсe.LanguagesLocalSource
import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language
import soft.divan.financemanager.feature.languages.languages_impl.domain.repository.LanguageRepository
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