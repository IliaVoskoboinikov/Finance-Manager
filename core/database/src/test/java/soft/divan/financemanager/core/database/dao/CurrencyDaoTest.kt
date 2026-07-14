package soft.divan.financemanager.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.CurrencyEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CurrencyDaoTest : RoomDaoTest() {

    private val dao get() = db.currencyDao()

    @Test
    fun `insertCurrencies stores and replaces by id`() = runTest {
        dao.insertCurrencies(
            listOf(
                CurrencyEntity(id = "rub", name = "Рубль"),
                CurrencyEntity(id = "usd", name = "Dollar")
            )
        )
        dao.insertCurrencies(listOf(CurrencyEntity(id = "rub", name = "Российский рубль")))

        val all = dao.getAllCurrencies().first()

        assertThat(all).hasSize(2)
        assertThat(all.first { it.id == "rub" }.name).isEqualTo("Российский рубль")
    }

    @Test
    fun `getCurrencyById returns currency or null`() = runTest {
        dao.insertCurrencies(listOf(CurrencyEntity(id = "rub", name = "Рубль")))

        assertThat(dao.getCurrencyById("rub")!!.name).isEqualTo("Рубль")
        assertThat(dao.getCurrencyById("missing")).isNull()
    }

    @Test
    fun `clearCurrencies empties the table`() = runTest {
        dao.insertCurrencies(listOf(CurrencyEntity(id = "rub", name = "Рубль")))

        dao.clearCurrencies()

        assertThat(dao.getAllCurrencies().first()).isEmpty()
    }
}
