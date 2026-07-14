package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

class TransactionDataMapperTest {

    private val createdAt = "2024-01-01T00:00:00Z"
    private val updatedAt = "2024-02-01T12:30:00Z"
    private val transactionDate = "2024-01-15T10:00:00Z"

    private val dto = TransactionDto(
        id = "server-t1",
        createdAt = createdAt,
        updatedAt = updatedAt,
        accountId = "server-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = transactionDate,
        comment = "lunch"
    )

    private val entity = TransactionEntity(
        localId = "local-t1",
        serverId = "server-t1",
        accountLocalId = "local-a1",
        type = TransactionType.EXPENSE.name,
        targetAccountLocalId = null,
        accountServerId = "server-a1",
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = transactionDate,
        comment = "lunch",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.SYNCED
    )

    private val domain = Transaction(
        id = "local-t1",
        accountLocalId = "local-a1",
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse(transactionDate),
        comment = "lunch",
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )

    @Test
    fun `TransactionDto toEntity maps all fields`() {
        val result = dto.toEntity(
            localId = "local-t1",
            accountLocalId = "local-a1",
            currencyId = "rub-id",
            type = TransactionType.EXPENSE,
            syncStatus = SyncStatus.PENDING_UPDATE
        )

        assertThat(result.localId).isEqualTo("local-t1")
        assertThat(result.serverId).isEqualTo("server-t1")
        assertThat(result.accountLocalId).isEqualTo("local-a1")
        assertThat(result.accountServerId).isEqualTo("server-a1")
        assertThat(result.categoryId).isEqualTo("cat-1")
        assertThat(result.currencyId).isEqualTo("rub-id")
        assertThat(result.amount).isEqualTo("42.42")
        assertThat(result.transactionDate).isEqualTo(transactionDate)
        assertThat(result.comment).isEqualTo("lunch")
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.updatedAt).isEqualTo(updatedAt)
        assertThat(result.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
        assertThat(result.type).isEqualTo("EXPENSE")
        assertThat(result.targetAccountLocalId).isNull()
    }

    @Test
    fun `TransactionDto toEntity replaces null comment with empty string`() {
        val result = dto.copy(comment = null).toEntity(
            localId = "local-t1",
            accountLocalId = "local-a1",
            currencyId = "rub-id",
            type = TransactionType.EXPENSE,
            syncStatus = SyncStatus.SYNCED
        )

        assertThat(result.comment).isEmpty()
    }

    @Test
    fun `TransactionEntity toUpdateDto maps fields with given server account id`() {
        val result = entity.toUpdateDto(accountServerId = "server-a2")

        assertThat(result.accountId).isEqualTo("server-a2")
        assertThat(result.categoryId).isEqualTo("cat-1")
        assertThat(result.amount).isEqualByComparingTo(BigDecimal("42.42"))
        assertThat(result.dateTime).isEqualTo(transactionDate)
        assertThat(result.comment).isEqualTo("lunch")
    }

    @Test
    fun `TransactionEntity toDto maps fields and keeps server id`() {
        val result = entity.toDto(accountServerId = "server-a1")

        assertThat(result.id).isEqualTo("server-t1")
        assertThat(result.accountId).isEqualTo("server-a1")
        assertThat(result.categoryId).isEqualTo("cat-1")
        assertThat(result.amount).isEqualByComparingTo(BigDecimal("42.42"))
        assertThat(result.dateTime).isEqualTo(transactionDate)
        assertThat(result.comment).isEqualTo("lunch")
    }

    @Test
    fun `TransactionEntity toDomain maps all fields and parses dates`() {
        assertThat(entity.toDomain()).isEqualTo(domain)
    }

    @Test
    fun `TransactionEntity toDomain resolves every transaction type`() {
        TransactionType.entries.forEach { type ->
            assertThat(entity.copy(type = type.name).toDomain().type).isEqualTo(type)
        }
    }

    @Test
    fun `Transaction toEntity maps all fields and formats dates`() {
        val result = domain.toEntity(
            serverId = "server-t1",
            accountServerId = "server-a1",
            syncStatus = SyncStatus.SYNCED
        )

        assertThat(result).isEqualTo(entity)
    }

    @Test
    fun `Transaction toEntity replaces null comment with empty string`() {
        val result = domain.copy(comment = null).toEntity(
            serverId = null,
            accountServerId = null,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        assertThat(result.comment).isEmpty()
        assertThat(result.serverId).isNull()
        assertThat(result.accountServerId).isNull()
    }

    @Test
    fun `Transaction toEntity keeps target account for transfers`() {
        val transfer = domain.copy(
            type = TransactionType.TRANSFER_OUT,
            targetAccountLocalId = "local-a2"
        )

        val result = transfer.toEntity(
            serverId = null,
            accountServerId = null,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        assertThat(result.targetAccountLocalId).isEqualTo("local-a2")
        assertThat(result.type).isEqualTo("TRANSFER_OUT")
    }

    @Test
    fun `entity to domain and back to entity preserves values`() {
        val roundTrip = entity.toDomain().toEntity(
            serverId = entity.serverId,
            accountServerId = entity.accountServerId,
            syncStatus = entity.syncStatus
        )

        assertThat(roundTrip).isEqualTo(entity)
    }
}
