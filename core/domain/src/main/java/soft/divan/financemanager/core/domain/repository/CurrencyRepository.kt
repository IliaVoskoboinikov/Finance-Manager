package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyRepository {
    fun getCurrency(): Flow<CurrencySymbol>
    suspend fun updateCurrency(currency: CurrencySymbol)
}