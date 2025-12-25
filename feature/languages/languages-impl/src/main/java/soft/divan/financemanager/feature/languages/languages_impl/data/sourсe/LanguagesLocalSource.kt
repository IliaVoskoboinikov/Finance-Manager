package soft.divan.financemanager.feature.languages.languages_impl.data.sourсe

import kotlinx.coroutines.flow.Flow

interface LanguagesLocalSource {
    fun observe(): Flow<String?>
    suspend fun save(code: String)
}