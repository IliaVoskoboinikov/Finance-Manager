package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Currency
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyRepository {
    fun getSelectedCurrency(): Flow<CurrencySymbol>
    suspend fun updateSelectedCurrency(currency: CurrencySymbol)
    fun getAllCurrencies(): Flow<List<Currency>>
    suspend fun getCurrencyById(id: String): Currency?
}
