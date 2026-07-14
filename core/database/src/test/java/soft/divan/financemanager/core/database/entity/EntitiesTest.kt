package soft.divan.financemanager.core.database.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.model.SyncStatus

class EntitiesTest {

    private val accountEntity = AccountEntity(
        localId = "local-1",
        serverId = "server-1",
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.SYNCED
    )

    private val transactionEntity = TransactionEntity(
        localId = "local-t1",
        serverId = null,
        accountLocalId = "local-1",
        type = "EXPENSE",
        targetAccountLocalId = null,
        accountServerId = null,
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = "2024-01-15T10:00:00Z",
        comment = "lunch",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.PENDING_CREATE
    )

    @Test
    fun `AccountEntity equality is structural`() {
        assertThat(accountEntity).isEqualTo(accountEntity.copy())
        assertThat(accountEntity.copy(balance = "0")).isNotEqualTo(accountEntity)
    }

    @Test
    fun `AccountEntity allows null server id for unsynced records`() {
        assertThat(accountEntity.copy(serverId = null).serverId).isNull()
    }

    @Test
    fun `TransactionEntity equality is structural`() {
        assertThat(transactionEntity).isEqualTo(transactionEntity.copy())
        assertThat(transactionEntity.copy(amount = "0")).isNotEqualTo(transactionEntity)
    }

    @Test
    fun `TransactionEntity keeps target account for transfers`() {
        val transfer = transactionEntity.copy(
            type = "TRANSFER_OUT",
            targetAccountLocalId = "local-2"
        )

        assertThat(transfer.targetAccountLocalId).isEqualTo("local-2")
    }

    @Test
    fun `CategoryEntity exposes its fields`() {
        val category = CategoryEntity(
            id = "1",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            name = "Food",
            emoji = "🍔",
            isIncome = false
        )

        assertThat(category.id).isEqualTo("1")
        assertThat(category.isIncome).isFalse()
        assertThat(category).isEqualTo(category.copy())
    }

    @Test
    fun `CurrencyEntity exposes its fields`() {
        val currency = CurrencyEntity(id = "rub-id", name = "Рубль")

        assertThat(currency.id).isEqualTo("rub-id")
        assertThat(currency.name).isEqualTo("Рубль")
        assertThat(currency).isEqualTo(currency.copy())
    }

    @Test
    fun `SyncStatus contains all lifecycle states`() {
        assertThat(SyncStatus.entries).containsExactly(
            SyncStatus.SYNCED,
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )
    }
}
