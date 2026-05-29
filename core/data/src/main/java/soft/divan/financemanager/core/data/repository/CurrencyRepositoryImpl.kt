package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.domain.model.Currency
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalDataSource: CurrencyLocalDataSource,
) : CurrencyRepository {

    override fun getSelectedCurrency(): Flow<CurrencySymbol> =
        currencyLocalDataSource.getSelectedCurrency()

    override suspend fun updateSelectedCurrency(currency: CurrencySymbol) =
        currencyLocalDataSource.updateSelectedCurrency(currency)

    override fun getAllCurrencies(): Flow<List<Currency>> =
        currencyLocalDataSource.getAllCurrencies().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCurrencyById(id: String): Currency? =
        currencyLocalDataSource.getCurrencyById(id)?.toDomain()


    private fun CurrencyEntity.toDomain() = Currency(
        id = id,
        name = name
    )


}
