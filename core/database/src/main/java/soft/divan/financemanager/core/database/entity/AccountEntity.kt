package soft.divan.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import soft.divan.financemanager.core.database.model.SyncStatus

@Entity(
    tableName = "account"
)
data class AccountEntity(
    @PrimaryKey
    val localId: String,
    val serverId: String?,
    val name: String,
    val balance: String,
    val currencyId: String,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)
