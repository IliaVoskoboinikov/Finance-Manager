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
    val syncStatus: SyncStatus,
    /**
     * Признак архивного («призрачного») счёта. Счёт нельзя удалить физически, пока на нём
     * есть операции, поэтому вместо удаления он архивируется: пропадает из списков и пикера
     * (фильтруется в data-слое при выдаче всех счетов), но остаётся в БД, чтобы история
     * операций могла подтянуть его имя/валюту. Обратной раз-архивации нет.
     */
    val archived: Boolean = false
)
