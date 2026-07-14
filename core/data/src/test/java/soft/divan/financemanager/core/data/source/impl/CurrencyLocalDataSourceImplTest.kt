package soft.divan.financemanager.core.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.dao.CurrencyDao
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.domain.model.CurrencySymbol

class CurrencyLocalDataSourceImplTest {

    private val dataStore = mockk<DataStore<Preferences>>()
    private val dao = mockk<CurrencyDao>(relaxUnitFun = true)
    private val dataSource = CurrencyLocalDataSourceImpl(dataStore, dao)

    private val key = stringPreferencesKey("app_currency")

    @Test
    fun `getSelectedCurrency returns stored currency`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(key to CurrencySymbol.EUR.id))

        assertThat(dataSource.getSelectedCurrency().first()).isEqualTo(CurrencySymbol.EUR)
    }

    @Test
    fun `getSelectedCurrency falls back to RUB when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(dataSource.getSelectedCurrency().first()).isEqualTo(CurrencySymbol.RUB)
    }

    @Test
    fun `getSelectedCurrency falls back to RUB for unknown id`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(key to "no-such-id"))

        assertThat(dataSource.getSelectedCurrency().first()).isEqualTo(CurrencySymbol.RUB)
    }

    @Test
    fun `updateSelectedCurrency stores currency id`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        dataSource.updateSelectedCurrency(CurrencySymbol.USD)

        val stored = transform.captured(emptyPreferences())
        assertThat(stored[key]).isEqualTo(CurrencySymbol.USD.id)
    }

    @Test
    fun `getAllCurrencies delegates to dao`() = runTest {
        val flow = flowOf(listOf(CurrencyEntity(id = "rub-id", name = "Рубль")))
        every { dao.getAllCurrencies() } returns flow

        assertThat(dataSource.getAllCurrencies()).isSameAs(flow)
    }

    @Test
    fun `saveCurrencies delegates to dao`() = runTest {
        val currencies = listOf(CurrencyEntity(id = "rub-id", name = "Рубль"))

        dataSource.saveCurrencies(currencies)

        coVerify(exactly = 1) { dao.insertCurrencies(currencies) }
    }

    @Test
    fun `getCurrencyById delegates to dao`() = runTest {
        val entity = CurrencyEntity(id = "rub-id", name = "Рубль")
        coEvery { dao.getCurrencyById("rub-id") } returns entity

        assertThat(dataSource.getCurrencyById("rub-id")).isEqualTo(entity)
    }

    @Test
    fun `getCurrencyById returns null for missing currency`() = runTest {
        coEvery { dao.getCurrencyById("missing") } returns null

        assertThat(dataSource.getCurrencyById("missing")).isNull()
    }
}
