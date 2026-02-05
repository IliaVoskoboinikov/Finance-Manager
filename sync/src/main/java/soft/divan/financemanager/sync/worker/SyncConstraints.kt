package soft.divan.financemanager.sync.worker

import androidx.work.Constraints
import androidx.work.NetworkType

/** Вся синхронизационная работа нуждается в интернет -подключении */
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

/**  По умолчанию синхронизируем раз в 4 часа*/
const val SYNCHRONIZATION_PERIOD_IN_HOURS = 4
