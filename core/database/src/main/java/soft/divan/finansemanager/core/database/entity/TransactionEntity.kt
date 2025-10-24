package soft.divan.finansemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: Int = 0,
    val accountId: Int,
    val categoryId: Int,
    val amount: String,
    val transactionDate: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false,
)
