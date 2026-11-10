package soft.divan.financemanager.feature.account.impl.precenter.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUi
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AccountMapperTest {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val createdAt = LocalDateTime.of(2024, 3, 7, 12, 30)
    private val updatedAt = LocalDateTime.of(2024, 4, 1, 9, 5)

    private val account = Account(
        id = "local-1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = createdAt.atZone(zone).toInstant(),
        updatedAt = updatedAt.atZone(zone).toInstant()
    )

    @Test
    fun `toUi maps fields`() {
        val ui = account.toUi()

        assertThat(ui.id).isEqualTo("local-1")
        assertThat(ui.name).isEqualTo("Cash")
        assertThat(ui.balance).isEqualTo("100.5")
        assertThat(ui.currencyId).isEqualTo("rub-id")
        assertThat(ui.createdAt).isEqualTo(account.createdAt)
        assertThat(ui.updatedAt).isEqualTo(account.updatedAt)
    }

    @Test
    fun `toUi strips trailing zeros from balance`() {
        assertThat(account.copy(balance = BigDecimal("10.00")).toUi().balance).isEqualTo("10")
        assertThat(account.copy(balance = BigDecimal("0.10")).toUi().balance).isEqualTo("0.1")
    }

    @Test
    fun `toDomain maps fields`() {
        val ui = AccountUi(
            id = "local-1",
            name = "Cash",
            balance = "100.50",
            currencyId = "rub-id",
            createdAt = createdAt.atZone(zone).toInstant(),
            updatedAt = updatedAt.atZone(zone).toInstant()
        )

        val domain = ui.toDomain()

        assertThat(domain.id).isEqualTo("local-1")
        assertThat(domain.balance).isEqualByComparingTo(BigDecimal("100.50"))
        assertThat(domain.createdAt).isEqualTo(createdAt.atZone(zone).toInstant())
        assertThat(domain.updatedAt).isEqualTo(updatedAt.atZone(zone).toInstant())
    }

    @Test
    fun `domain to ui and back preserves timestamps exactly`() {
        val withSeconds = account.copy(
            createdAt = Instant.parse("2024-03-07T12:30:15.123Z"),
            updatedAt = Instant.parse("2024-04-01T09:05:42.456Z")
        )

        val roundTrip = withSeconds.toUi().toDomain()

        assertThat(roundTrip.createdAt).isEqualTo(withSeconds.createdAt)
        assertThat(roundTrip.updatedAt).isEqualTo(withSeconds.updatedAt)
    }

    @Test
    fun `domain to ui and back preserves values`() {
        val roundTrip = account.toUi().toDomain()

        assertThat(roundTrip.id).isEqualTo(account.id)
        assertThat(roundTrip.name).isEqualTo(account.name)
        assertThat(roundTrip.balance).isEqualByComparingTo(account.balance)
        assertThat(roundTrip.createdAt).isEqualTo(account.createdAt)
        assertThat(roundTrip.updatedAt).isEqualTo(account.updatedAt)
    }
}
