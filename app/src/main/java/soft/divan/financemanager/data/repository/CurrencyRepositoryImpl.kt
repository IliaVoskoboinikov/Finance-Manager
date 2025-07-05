package soft.divan.financemanager.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.domain.model.CurrencyCode
import soft.divan.financemanager.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalDataSource: CurrencyLocalDataSource
) : CurrencyRepository {

    override fun getCurrency(): Flow<CurrencyCode> = currencyLocalDataSource.getCurrency()

    override suspend fun updateCurrency(currency: CurrencyCode) {
        currencyLocalDataSource.updateCurrency(currency)
    }
}