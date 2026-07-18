package soft.divan.financemanager.core.data.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal

class AccountDtoTest {

    private val dto = AccountDto(
        id = "server-1",
        userId = "user-1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-02-01T00:00:00Z"
    )

    @Test
    fun `equality is structural`() {
        assertThat(dto).isEqualTo(dto.copy())
        assertThat(dto.copy(name = "Card")).isNotEqualTo(dto)
    }

    @Test
    fun `copy changes only the requested field`() {
        val renamed = dto.copy(name = "Card")

        assertThat(renamed.name).isEqualTo("Card")
        assertThat(renamed.id).isEqualTo("server-1")
        assertThat(renamed.balance).isEqualByComparingTo(BigDecimal("100.50"))
    }

    @Test
    fun `exposes all fields`() {
        assertThat(dto.userId).isEqualTo("user-1")
        assertThat(dto.currencyId).isEqualTo("rub-id")
        assertThat(dto.createdAt).isEqualTo("2024-01-01T00:00:00Z")
        assertThat(dto.updatedAt).isEqualTo("2024-02-01T00:00:00Z")
    }

    @Test
    fun `archived defaults to false and is settable`() {
        assertThat(dto.archived).isFalse()
        assertThat(dto.copy(archived = true).archived).isTrue()
    }
}
