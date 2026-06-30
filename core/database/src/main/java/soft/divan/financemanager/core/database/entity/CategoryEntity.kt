package soft.divan.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val name: String,
    val emoji: String,
    val isIncome: Boolean
)
