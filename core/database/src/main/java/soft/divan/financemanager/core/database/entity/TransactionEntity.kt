package soft.divan.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import soft.divan.financemanager.core.database.model.SyncStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val localId: String,
    val serverId: String?,
    val accountLocalId: String, // todo может быть не хранить accountServerId тут вообще
    val type: String,
    val targetAccountLocalId: String?,
    val accountServerId: String?,
    val categoryId: String,
    val currencyId: String,
    val amount: String,
    val transactionDate: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)
