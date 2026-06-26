package soft.divan.financemanager.core.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrencySymbolTest {

    @Test
    fun `fromCode returns symbol for known code`() {
        assertThat(CurrencySymbol.fromCode("USD")).isEqualTo("$")
        assertThat(CurrencySymbol.fromCode("EUR")).isEqualTo("€")
        assertThat(CurrencySymbol.fromCode("RUB")).isEqualTo("₽")
    }

    @Test
    fun `fromCode ignores case`() {
        assertThat(CurrencySymbol.fromCode("usd")).isEqualTo("$")
        assertThat(CurrencySymbol.fromCode("eUr")).isEqualTo("€")
    }

    @Test
    fun `fromCode returns input for unknown code`() {
        assertThat(CurrencySymbol.fromCode("GBP")).isEqualTo("GBP")
    }

    @Test
    fun `fromSymbol returns code for known symbol`() {
        assertThat(CurrencySymbol.fromSymbol("$")).isEqualTo("USD")
        assertThat(CurrencySymbol.fromSymbol("€")).isEqualTo("EUR")
        assertThat(CurrencySymbol.fromSymbol("₽")).isEqualTo("RUB")
    }

    @Test
    fun `fromSymbol returns input for unknown symbol`() {
        assertThat(CurrencySymbol.fromSymbol("£")).isEqualTo("£")
    }

    @Test
    fun `enum exposes code and symbol`() {
        assertThat(CurrencySymbol.USD.code).isEqualTo("USD")
        assertThat(CurrencySymbol.USD.symbol).isEqualTo("$")
        assertThat(CurrencySymbol.entries).hasSize(3)
    }
}
