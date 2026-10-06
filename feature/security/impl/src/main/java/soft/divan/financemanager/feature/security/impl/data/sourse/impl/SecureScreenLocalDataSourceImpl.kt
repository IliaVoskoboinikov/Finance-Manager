package soft.divan.financemanager.feature.security.impl.data.sourse.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.security.impl.data.sourse.SecureScreenLocalDataSource
import soft.divan.financemanager.feature.security.impl.di.SecuritySettingsDataStore
import javax.inject.Inject

private val KEY_SECURE_SCREEN = booleanPreferencesKey("secure_screen_enabled")

/**
 * Защита экрана включена по умолчанию: финансовые данные не должны попадать ни в скриншоты,
 * ни в превью списка недавних приложений, пока пользователь сам не решит иначе.
 */
class SecureScreenLocalDataSourceImpl @Inject constructor(
    @param:SecuritySettingsDataStore private val dataStore: DataStore<Preferences>
) : SecureScreenLocalDataSource {

    override fun observeEnabled(): Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[KEY_SECURE_SCREEN] ?: true }
        .distinctUntilChanged()

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_SECURE_SCREEN] = enabled }
    }
}
