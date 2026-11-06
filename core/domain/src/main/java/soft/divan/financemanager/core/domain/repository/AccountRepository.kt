package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult
import java.math.BigDecimal

interface AccountRepository {
    suspend fun create(account: Account): DomainResult<Unit>

    /**
     * Счета для списков и пикера: без архивных (`Deleted`) и без ждущих удаления на сервере.
     *
     * Для выборок операций не подходит — операции архивного счёта из них пропали бы
     * (см. [getAllIncludingArchived]).
     */
    fun getAll(): Flow<DomainResult<List<Account>>>

    /**
     * Все локальные счета, включая архивные и ждущие удаления.
     *
     * Нужен выборкам истории и аналитики: архивный счёт скрыт из списков, но его операции
     * остаются в истории, и суммы за прошлые периоды не должны меняться после архивации
     * (см. `docs/account-archive.md`).
     */
    fun getAllIncludingArchived(): Flow<DomainResult<List<Account>>>
    suspend fun update(account: Account): DomainResult<Unit>

    /**
     * Обновляет баланс счёта только локально: без пуша на сервер и без смены syncStatus.
     *
     * Используется при создании/изменении/удалении транзакции: сервер сам пересчитывает
     * баланс счёта при пуше самой транзакции, поэтому отправка PUT /account с балансом
     * привела бы к двойному применению суммы. Локальный пересчёт нужен для offline-first UI,
     * а авторитетный серверный баланс приедет следующим pull'ом (last-write-wins).
     */
    suspend fun updateBalanceLocal(accountId: String, balance: BigDecimal): DomainResult<Unit>

    suspend fun delete(id: String): DomainResult<Unit>
    suspend fun getById(id: String): DomainResult<Account>
}
