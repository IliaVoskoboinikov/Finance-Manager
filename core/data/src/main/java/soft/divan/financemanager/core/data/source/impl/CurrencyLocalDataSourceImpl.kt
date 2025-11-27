package soft.divan.financemanager.core.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import javax.inject.Inject

class CurrencyLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CurrencyLocalDataSource {

    private val key = stringPreferencesKey("app_currency")

    override fun getCurrency(): Flow<CurrencySymbol> {
        return dataStore.data.map { prefs ->
            prefs[key]?.toEnumOrNull<CurrencySymbol>() ?: CurrencySymbol.RUB
        }
    }

    override suspend fun updateCurrency(currency: CurrencySymbol) {
        dataStore.edit { prefs ->
            prefs[key] = currency.code
        }
    }

    inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
        return enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) }
    }
}