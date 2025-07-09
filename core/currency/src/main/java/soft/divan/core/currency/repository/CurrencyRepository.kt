package soft.divan.core.currency.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.core.currency.CurrencyCode

interface CurrencyRepository {
    fun getCurrency(): Flow<CurrencyCode>
    suspend fun updateCurrency(currency: CurrencyCode)
}