package soft.divan.financemanager.feature.languages.impl.data.source

import kotlinx.coroutines.flow.Flow

interface LanguagesLocalSource {
    fun observe(): Flow<String?>
    suspend fun save(code: String)
}
// Revue me>>
