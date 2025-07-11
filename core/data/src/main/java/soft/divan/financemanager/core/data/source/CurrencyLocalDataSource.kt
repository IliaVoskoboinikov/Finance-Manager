package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencyCode

interface CurrencyLocalDataSource {
    fun getCurrency(): Flow<CurrencyCode>
    suspend fun updateCurrency(currency: CurrencyCode)
}