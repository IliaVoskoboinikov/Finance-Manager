package soft.divan.financemanager.core.database.util

import androidx.room.TypeConverter
import soft.divan.financemanager.core.database.model.SyncStatus

class Converters {

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}
