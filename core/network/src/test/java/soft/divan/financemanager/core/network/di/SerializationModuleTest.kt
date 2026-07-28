package soft.divan.financemanager.core.network.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal

class SerializationModuleTest {

    private val gson = SerializationModule.provideGson()

    @Test
    fun `gson keeps BigDecimal precision on read`() {
        val value = gson.fromJson("\"12345678901234.56\"", BigDecimal::class.java)

        assertThat(value).isEqualByComparingTo(BigDecimal("12345678901234.56"))
    }

    @Test
    fun `gson writes BigDecimal as plain value`() {
        val json = gson.toJson(BigDecimal("100.50"))

        assertThat(json).isEqualTo("100.50")
    }

    @Test
    fun `gson round-trips money values without drift`() {
        val original = BigDecimal("0.1")

        val restored = gson.fromJson(gson.toJson(original), BigDecimal::class.java)

        assertThat(restored).isEqualByComparingTo(original)
    }
}
