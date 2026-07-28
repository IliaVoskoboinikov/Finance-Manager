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
     * Статус счёта — имя серверного enum `EStatus` (`Active`/`Hidden`/`Deleted`), зеркалит
     * `AccountStatus` из доменного слоя (парсинг и обратное преобразование — в data-слое, чтобы
     * не заводить зависимость `core:database` → `core:domain`).
     *
     * `Deleted` — «архивный» счёт: счёт с операциями нельзя удалить физически, поэтому он
     * архивируется — пропадает из списков и пикера (фильтруется в data-слое), но остаётся в БД,
     * чтобы история операций могла подтянуть его имя/валюту. Обратной раз-архивации нет.
     */
    val status: String = "Active"
)
