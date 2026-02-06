package soft.divan.financemanager.feature.languages.impl.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.languages.impl.data.source.LanguagesLocalSource
import javax.inject.Inject

val KEY_LANGUAGE = stringPreferencesKey("app_language")

class LanguageLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LanguagesLocalSource {

    override fun observe(): Flow<String?> {
        return dataStore.data.map { it[KEY_LANGUAGE] }
    }

    override suspend fun save(code: String) {
        dataStore.edit { it[KEY_LANGUAGE] = code }
    }
}
// Revue me>>
