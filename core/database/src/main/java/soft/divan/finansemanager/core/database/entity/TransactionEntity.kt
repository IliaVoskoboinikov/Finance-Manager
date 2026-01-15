package soft.divan.finansemanager.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import soft.divan.finansemanager.core.database.model.SyncStatus

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["serverId"], unique = true)] //todo
)
data class TransactionEntity(
    @PrimaryKey
    val localId: String,
    val serverId: Int?,
    val accountLocalId: String,
    //todo может быть не хранить accountServerId тут вообще
    val accountServerId: Int?,
    val categoryId: Int,
    val currencyCode: String,
    val amount: String,
    val transactionDate: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)
