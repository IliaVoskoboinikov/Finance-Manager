package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.domain.model.Account
import java.math.BigDecimal
import java.time.Instant

/**
 * Проверяет, что баланс проходит через мапперы без потери точности (никаких `toDouble`).
 */
class AccountMoneyMappingTest {

    private val precise = "12345678901234.56"

    @Test
    fun `Account to CreateDto keeps exact balance`() {
        val account = Account(
            id = "1",
            name = "Test",
            balance = BigDecimal(precise),
            currencyId = "USD",
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        assertThat(account.toDto().balance).isEqualByComparingTo(BigDecimal(precise))
    }

    @Test
    fun `AccountEntity to UpdateDto parses balance string exactly`() {
        val entity = accountEntity(balance = precise)

        assertThat(entity.toUpdateDto().balance).isEqualByComparingTo(BigDecimal(precise))
    }

    @Test
    fun `AccountDto to entity stores balance as plain string`() {
        val dto = AccountDto(
            id = "1",
            userId = "u1",
            name = "Test",
            balance = BigDecimal(precise),
            currencyId = "USD",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        assertThat(dto.toEntity(localId = "1", syncStatus = SyncStatus.SYNCED).balance)
            .isEqualTo(precise)
    }

    private fun accountEntity(balance: String) = AccountEntity(
        localId = "1",
        serverId = "s1",
        name = "Test",
        balance = balance,
        currencyId = "USD",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.SYNCED
    )
}
