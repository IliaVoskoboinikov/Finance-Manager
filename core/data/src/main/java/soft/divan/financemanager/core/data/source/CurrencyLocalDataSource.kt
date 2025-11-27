package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyLocalDataSource {
    fun getCurrency(): Flow<CurrencySymbol>
    suspend fun updateCurrency(currency: CurrencySymbol)
}