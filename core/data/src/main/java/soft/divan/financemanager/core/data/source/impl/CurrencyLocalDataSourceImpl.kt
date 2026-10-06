package soft.divan.financemanager.core.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import javax.inject.Inject

class CurrencyLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val holder: DatabaseHolder
) : CurrencyLocalDataSource {

    private val key = stringPreferencesKey("app_currency")

    override fun getSelectedCurrency(): Flow<CurrencySymbol> {
        return dataStore.data.map { prefs ->
            prefs[key]?.let { CurrencySymbol.getById(it) } ?: CurrencySymbol.RUB
        }
    }

    override suspend fun updateSelectedCurrency(currency: CurrencySymbol) {
        dataStore.edit { prefs ->
            prefs[key] = currency.id
        }
    }

    override fun getAllCurrencies(): Flow<List<CurrencyEntity>> =
        holder.observe { it.currencyDao().getAllCurrencies() }

    override suspend fun saveCurrencies(currencies: List<CurrencyEntity>) =
        holder.withDatabase { it.currencyDao().insertCurrencies(currencies) }

    override suspend fun getCurrencyById(id: String): CurrencyEntity? =
        holder.withDatabase { it.currencyDao().getCurrencyById(id) }
}
