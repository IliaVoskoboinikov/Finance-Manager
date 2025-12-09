package soft.divan.finansemanager.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import soft.divan.finansemanager.core.database.model.SyncStatus

@Entity(
    tableName = "account",
    indices = [Index(value = ["serverId"], unique = true)] //todo
)
data class AccountEntity(
    @PrimaryKey
    val localId: String,
    val serverId: Int?,
    val name: String,
    val balance: String,
    val currency: String,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)
