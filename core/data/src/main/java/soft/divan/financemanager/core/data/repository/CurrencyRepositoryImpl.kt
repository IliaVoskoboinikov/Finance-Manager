package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalDataSource: CurrencyLocalDataSource
) : CurrencyRepository {

    override fun getCurrency(): Flow<CurrencyCode> = currencyLocalDataSource.getCurrency()

    override suspend fun updateCurrency(currency: CurrencyCode) {
        currencyLocalDataSource.updateCurrency(currency)
    }
}