package soft.divan.financemanager.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.CurrencyCode

interface CurrencyLocalDataSource {
    fun getCurrency(): Flow<CurrencyCode>
    suspend fun updateCurrency(currency: CurrencyCode)
}