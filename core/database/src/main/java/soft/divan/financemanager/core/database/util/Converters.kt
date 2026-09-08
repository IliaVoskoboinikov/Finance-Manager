package soft.divan.financemanager.core.database.util

import androidx.room.TypeConverter
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
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

    @TypeConverter
    fun fromOutboxEntityType(type: OutboxEntityType): String = type.name

    @TypeConverter
    fun toOutboxEntityType(value: String): OutboxEntityType = OutboxEntityType.valueOf(value)

    @TypeConverter
    fun fromOutboxOperation(operation: OutboxOperation): String = operation.name

    @TypeConverter
    fun toOutboxOperation(value: String): OutboxOperation = OutboxOperation.valueOf(value)

    @TypeConverter
    fun fromOutboxStatus(status: OutboxStatus): String = status.name

    /**
     * Нераспознанный статус трактуется как [OutboxStatus.FAILED], а не как «можно отправлять»:
     * непонятную запись безопаснее показать в dead-letter, чем вслепую отправить на сервер.
     *
     * Обратная сторона — записи с испорченным статусом не потеряются молча.
     */
    @TypeConverter
    fun toOutboxStatus(value: String): OutboxStatus =
        runCatching { OutboxStatus.valueOf(value) }.getOrDefault(OutboxStatus.FAILED)
}
