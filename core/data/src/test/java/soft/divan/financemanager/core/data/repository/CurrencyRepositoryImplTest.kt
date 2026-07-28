package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.domain.model.Currency
import soft.divan.financemanager.core.domain.model.CurrencySymbol

class CurrencyRepositoryImplTest {

    private val localDataSource = mockk<CurrencyLocalDataSource>(relaxUnitFun = true)

    private val repository = CurrencyRepositoryImpl(localDataSource)

    @Test
    fun `getSelectedCurrency delegates to local source`() = runTest {
        every { localDataSource.getSelectedCurrency() } returns flowOf(CurrencySymbol.EUR)

        assertThat(repository.getSelectedCurrency().first()).isEqualTo(CurrencySymbol.EUR)
    }

    @Test
    fun `updateSelectedCurrency delegates to local source`() = runTest {
        repository.updateSelectedCurrency(CurrencySymbol.USD)

        coVerify(exactly = 1) { localDataSource.updateSelectedCurrency(CurrencySymbol.USD) }
    }

    @Test
    fun `getAllCurrencies maps entities to domain models`() = runTest {
        every { localDataSource.getAllCurrencies() } returns flowOf(
            listOf(
                CurrencyEntity(id = "rub-id", name = "Российский рубль"),
                CurrencyEntity(id = "usd-id", name = "US Dollar")
            )
        )

        val currencies = repository.getAllCurrencies().first()

        assertThat(currencies).containsExactly(
            Currency(id = "rub-id", name = "Российский рубль"),
            Currency(id = "usd-id", name = "US Dollar")
        )
    }

    @Test
    fun `getCurrencyById maps entity to domain model`() = runTest {
        coEvery { localDataSource.getCurrencyById("rub-id") } returns
            CurrencyEntity(id = "rub-id", name = "Российский рубль")

        val currency = repository.getCurrencyById("rub-id")

        assertThat(currency).isEqualTo(Currency(id = "rub-id", name = "Российский рубль"))
    }

    @Test
    fun `getCurrencyById returns null for unknown id`() = runTest {
        coEvery { localDataSource.getCurrencyById("missing") } returns null

        assertThat(repository.getCurrencyById("missing")).isNull()
    }
}
