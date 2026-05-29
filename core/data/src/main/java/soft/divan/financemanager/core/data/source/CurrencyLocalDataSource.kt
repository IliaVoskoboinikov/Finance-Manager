package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyLocalDataSource {
    fun getSelectedCurrency(): Flow<CurrencySymbol>
    suspend fun updateSelectedCurrency(currency: CurrencySymbol)
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    suspend fun saveCurrencies(currencies: List<CurrencyEntity>)
    suspend fun getCurrencyById(id: String): CurrencyEntity?
}
