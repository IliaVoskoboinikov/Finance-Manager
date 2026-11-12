package soft.divan.financemanager.core.data.testing

import soft.divan.financemanager.core.database.holder.DataGenerationGuard

/**
 * Привязка, которая ничего не проверяет, — для тестов логики синка на моках, где базы нет.
 * Само поколение данных проверяют `RoomDatabaseHolderTest` и `SyncAfterWipeTest`.
 */
object PassThroughDataGuard : DataGenerationGuard {
    override suspend fun <T> bindToCurrentData(block: suspend () -> T): T = block()
}
