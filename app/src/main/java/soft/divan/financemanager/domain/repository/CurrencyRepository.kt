package soft.divan.financemanager.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.CurrencyCode

interface CurrencyRepository {
    fun getCurrency(): Flow<CurrencyCode>
    suspend fun updateCurrency(currency: CurrencyCode)
}