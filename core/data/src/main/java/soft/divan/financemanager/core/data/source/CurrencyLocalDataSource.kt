package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyLocalDataSource {
    fun get(): Flow<CurrencySymbol>
    suspend fun update(currency: CurrencySymbol)
}