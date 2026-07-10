package soft.divan.financemanager.sync.worker

import androidx.work.Constraints
import androidx.work.NetworkType

/** Вся синхронизационная работа нуждается в интернет-подключении */
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

/**  По умолчанию синхронизируем раз в 4 часа*/
const val SYNCHRONIZATION_PERIOD_IN_HOURS = 4

/**
 * Допустимый диапазон интервала синка в часах. Совпадает с диапазоном слайдера на экране
 * синхронизации и защищает WorkManager: интервал < минимума он молча зажимает до 15 минут,
 * а 0/отрицательное значение приводит к некорректному `PeriodicWorkRequest`.
 */
const val MIN_SYNC_INTERVAL_HOURS = 1
const val MAX_SYNC_INTERVAL_HOURS = 24
