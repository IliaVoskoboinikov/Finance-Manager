package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalDataSource: CurrencyLocalDataSource
) : CurrencyRepository {

    override fun get(): Flow<CurrencySymbol> = currencyLocalDataSource.get()

    override suspend fun update(currency: CurrencySymbol) = currencyLocalDataSource.update(currency)
}
// Revue me>>
