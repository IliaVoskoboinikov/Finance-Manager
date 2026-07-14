package soft.divan.financemanager.core.network.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal

class BigDecimalTypeAdapterTest {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter())
        .create()

    private data class Money(val amount: BigDecimal?)

    @Test
    fun `serializes as an unquoted plain number`() {
        val json = gson.toJson(Money(BigDecimal("99999999.99")))

        assertThat(json).isEqualTo("""{"amount":99999999.99}""")
        // именно число, а не строка в кавычках
        assertThat(json).doesNotContain("\"99999999.99\"")
    }

    @Test
    fun `does not use scientific notation for large values`() {
        val json = gson.toJson(Money(BigDecimal("10000000")))

        assertThat(json).isEqualTo("""{"amount":10000000}""")
    }

    @Test
    fun `round trips values that Double would corrupt`() {
        listOf("0.1", "0.2", "0.3", "99999999.99", "12345678901234.56", "0.00000001")
            .forEach { raw ->
                val restored = gson.fromJson(
                    gson.toJson(Money(BigDecimal(raw))),
                    Money::class.java
                ).amount

                assertThat(restored).isEqualByComparingTo(BigDecimal(raw))
            }
    }

    @Test
    fun `reads a raw JSON number into an exact BigDecimal`() {
        val restored = gson.fromJson("""{"amount":0.30000000000000004}""", Money::class.java)

        assertThat(restored.amount).isEqualByComparingTo(BigDecimal("0.30000000000000004"))
    }

    @Test
    fun `handles null`() {
        val json = gson.toJson(Money(null))
        assertThat(gson.fromJson(json, Money::class.java).amount).isNull()
    }

    // Прямое обращение к адаптеру гарантирует прохождение обеих null-веток read/write,
    // которые Gson при работе через рефлективный маппер может короткозамкнуть.
    private val adapter = BigDecimalTypeAdapter()

    @Test
    fun `read returns null for a JSON null token`() {
        val reader = JsonReader(StringReader("null"))

        assertThat(adapter.read(reader)).isNull()
    }

    @Test
    fun `read parses a raw number token into exact BigDecimal`() {
        val reader = JsonReader(StringReader("12345678901234.56"))

        assertThat(adapter.read(reader)).isEqualByComparingTo(BigDecimal("12345678901234.56"))
    }

    @Test
    fun `write emits null literal for null value`() {
        val sw = StringWriter()

        adapter.write(JsonWriter(sw), null)

        assertThat(sw.toString()).isEqualTo("null")
    }

    @Test
    fun `write emits plain unquoted number`() {
        val sw = StringWriter()

        adapter.write(JsonWriter(sw), BigDecimal("100.50"))

        assertThat(sw.toString()).isEqualTo("100.50")
    }
}
