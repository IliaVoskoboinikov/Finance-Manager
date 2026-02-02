package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface CurrencyRepository {
    fun get(): Flow<CurrencySymbol>
    suspend fun update(currency: CurrencySymbol)
}