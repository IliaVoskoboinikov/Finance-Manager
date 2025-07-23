package soft.divan.finansemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val name: String,
    val balance: BigDecimal,
    val currency: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
