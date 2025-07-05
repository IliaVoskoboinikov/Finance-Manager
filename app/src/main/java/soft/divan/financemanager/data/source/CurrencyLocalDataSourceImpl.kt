package soft.divan.financemanager.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.domain.model.CurrencyCode
import javax.inject.Inject

class CurrencyLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CurrencyLocalDataSource {

    private val key = stringPreferencesKey("app_currency")

    override fun getCurrency(): Flow<CurrencyCode> {
        return dataStore.data.map { CurrencyCode(it[key] ?: "RUB") }
    }

    override suspend fun updateCurrency(currency: CurrencyCode) {
        dataStore.edit { prefs ->
            prefs[key] = currency.code
        }
    }

}