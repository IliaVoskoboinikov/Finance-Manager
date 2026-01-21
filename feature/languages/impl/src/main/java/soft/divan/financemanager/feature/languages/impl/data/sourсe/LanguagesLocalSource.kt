package soft.divan.financemanager.feature.languages.impl.data.sourсe

import kotlinx.coroutines.flow.Flow

interface LanguagesLocalSource {
    fun observe(): Flow<String?>
    suspend fun save(code: String)
}