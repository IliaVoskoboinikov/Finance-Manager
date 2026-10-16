package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult

/**
 * Доступ к зашифрованным локальным данным: открыты ли они, как их открыть, запереть и стереть.
 *
 * PIN передаётся строкой только внутрь: ни ключ базы, ни сохранённые секреты наружу не выходят —
 * слой представления получает лишь итог операции. Сменой уровня защиты занимается
 * [DataProtectionRepository].
 */
interface LocalDataAccessRepository {

    /** Доступны ли данные; до окончания [initialize] — [LocalDataState.Initializing]. */
    val state: StateFlow<LocalDataState>

    /**
     * Читает ключи и открывает базу, если уровень позволяет сделать это без пользователя.
     * Вызывается при старте процесса; повторный вызов после [LocalDataState.KeyLost] — попытка
     * ещё раз.
     */
    suspend fun initialize()

    /**
     * Открывает данные уровня PIN. Каждая ошибка учитывается: паузы, а после десятой — стирание
     * данных и выход из аккаунта.
     */
    suspend fun unlockWithPin(pin: String): PinUnlockResult

    /** Сколько попыток PIN осталось и не запрещён ли ввод сейчас. */
    suspend fun pinLockout(): PinLockout

    /** Закрывает базу, если уровень требует PIN; иначе ничего не делает. */
    suspend fun lock()

    /**
     * Стирает данные и ключи и создаёт пустую базу с уровнем защиты «ключ устройства».
     * [signOut] — заодно выйти из аккаунта: без этого «забыл PIN» стал бы обходом шифрования,
     * ведь данные вернулись бы с сервера по сохранённой сессии.
     */
    suspend fun wipe(signOut: Boolean)

    /**
     * Выход из [LocalDataState.KeyLost]: стирает недоступные данные и начинает заново. Если данные
     * были под PIN (или уровень уже не прочитать), заодно выходит из аккаунта — по той же причине,
     * что и «забыл PIN».
     *
     * @return `true`, если вместе с данными выполнен выход из аккаунта.
     */
    suspend fun recover(): Boolean
}
