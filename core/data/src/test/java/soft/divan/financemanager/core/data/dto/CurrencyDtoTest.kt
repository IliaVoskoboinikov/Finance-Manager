package soft.divan.financemanager.core.data.dto

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrencyDtoTest {

    @Test
    fun `equality is structural`() {
        assertThat(CurrencyDto(id = "rub-id", name = "Рубль"))
            .isEqualTo(CurrencyDto(id = "rub-id", name = "Рубль"))
        assertThat(CurrencyDto(id = "rub-id", name = "Рубль"))
            .isNotEqualTo(CurrencyDto(id = "usd-id", name = "Доллар"))
    }

    @Test
    fun `deserializes from api json field names`() {
        val dto = Gson().fromJson(
            """{"id":"rub-id","name":"Российский рубль"}""",
            CurrencyDto::class.java
        )

        assertThat(dto.id).isEqualTo("rub-id")
        assertThat(dto.name).isEqualTo("Российский рубль")
    }
}
