package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencyCode

interface CurrencyRepository {
    fun getCurrency(): Flow<CurrencyCode>
    suspend fun updateCurrency(currency: CurrencyCode)
}