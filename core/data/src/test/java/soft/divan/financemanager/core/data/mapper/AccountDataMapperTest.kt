package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Account
import java.math.BigDecimal
import java.time.Instant

class AccountDataMapperTest {

    private val createdAt = "2024-01-01T00:00:00Z"
    private val updatedAt = "2024-02-01T12:30:00Z"

    private val dto = AccountDto(
        id = "server-1",
        userId = "user-1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private val entity = AccountEntity(
        localId = "local-1",
        serverId = "server-1",
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.SYNCED
    )

    private val account = Account(
        id = "local-1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )

    @Test
    fun `AccountDto toEntity maps all fields`() {
        val result = dto.toEntity(localId = "local-1", syncStatus = SyncStatus.PENDING_UPDATE)

        assertThat(result.localId).isEqualTo("local-1")
        assertThat(result.serverId).isEqualTo("server-1")
        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualTo("100.50")
        assertThat(result.currencyId).isEqualTo("rub-id")
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.updatedAt).isEqualTo(updatedAt)
        assertThat(result.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
    }

    @Test
    fun `Account toEntity maps all fields and formats dates`() {
        val result = account.toEntity(serverId = "server-1", syncStatus = SyncStatus.PENDING_CREATE)

        assertThat(result.localId).isEqualTo("local-1")
        assertThat(result.serverId).isEqualTo("server-1")
        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualTo("100.50")
        assertThat(result.currencyId).isEqualTo("rub-id")
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.updatedAt).isEqualTo(updatedAt)
        assertThat(result.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
    }

    @Test
    fun `Account toEntity allows null serverId`() {
        val result = account.toEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)

        assertThat(result.serverId).isNull()
    }

    @Test
    fun `AccountEntity toDomain maps all fields and parses dates`() {
        val result = entity.toDomain()

        assertThat(result.id).isEqualTo("local-1")
        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualByComparingTo(BigDecimal("100.50"))
        assertThat(result.currencyId).isEqualTo("rub-id")
        assertThat(result.createdAt).isEqualTo(Instant.parse(createdAt))
        assertThat(result.updatedAt).isEqualTo(Instant.parse(updatedAt))
    }

    @Test
    fun `Account toDto maps name balance and currency`() {
        val result = account.toDto()

        assertThat(result.id).isNull()
        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualByComparingTo(BigDecimal("100.50"))
        assertThat(result.currencyId).isEqualTo("rub-id")
    }

    @Test
    fun `AccountEntity toUpdateDto maps name balance and currency`() {
        val result = entity.toUpdateDto()

        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualByComparingTo(BigDecimal("100.50"))
        assertThat(result.currencyId).isEqualTo("rub-id")
    }

    @Test
    fun `AccountEntity toDto maps name balance and currency`() {
        val result = entity.toDto()

        assertThat(result.id).isNull()
        assertThat(result.name).isEqualTo("Cash")
        assertThat(result.balance).isEqualByComparingTo(BigDecimal("100.50"))
        assertThat(result.currencyId).isEqualTo("rub-id")
    }

    @Test
    fun `entity to domain and back to entity preserves values`() {
        val roundTrip = entity.toDomain().toEntity(
            serverId = entity.serverId,
            syncStatus = entity.syncStatus
        )

        assertThat(roundTrip).isEqualTo(entity)
    }
}
