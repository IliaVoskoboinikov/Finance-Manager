package soft.divan.financemanager.core.database.util

import androidx.room.TypeConverter
import soft.divan.financemanager.core.database.model.SyncStatus

class Converters {

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    /**
     * Нераспознанное значение (повреждение строки/дрейф enum между версиями) не роняет
     * чтение из Room, а трактуется как [SyncStatus.SYNCED]: не пушим непонятный статус на
     * сервер, а ближайший pull по last-write-wins восстановит корректное состояние.
     */
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return runCatching { SyncStatus.valueOf(value) }.getOrDefault(SyncStatus.SYNCED)
    }
}
