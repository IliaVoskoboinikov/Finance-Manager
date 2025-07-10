package soft.divan.financemanager.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.core.currency.CurrencyCode
import soft.divan.core.currency.repository.CurrencyRepository
import soft.divan.financemanager.data.source.CurrencyLocalDataSource
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalDataSource: CurrencyLocalDataSource
) : CurrencyRepository {

    override fun getCurrency(): Flow<CurrencyCode> = currencyLocalDataSource.getCurrency()

    override suspend fun updateCurrency(currency: CurrencyCode) {
        currencyLocalDataSource.updateCurrency(currency)
    }
}