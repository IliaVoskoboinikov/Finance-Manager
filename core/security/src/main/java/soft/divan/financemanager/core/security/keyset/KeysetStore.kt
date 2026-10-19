package soft.divan.financemanager.core.security.keyset

import kotlinx.coroutines.flow.Flow

/**
 * Хранилище [Keyset] и учёта попыток PIN.
 *
 * Запись набора атомарна целиком: после сбоя остаётся либо старый набор, либо новый, но не их
 * смесь. На этом держится безопасная смена уровня — см. `docs/encryption.md`.
 *
 * Лежит в `noBackupFilesDir`: завёрнутые ключи бессмысленны на другом устройстве (ключи Keystore
 * туда не переезжают), а открытый ключ уровня L0 туда попадать не должен.
 */
interface KeysetStore {

    /**
     * Сохранённый набор или `null`, если его нет (первый запуск или испорченный файл).
     *
     * @throws soft.divan.financemanager.core.security.keystore.KeyUnavailableException набор
     *   есть, но прочитать его нельзя.
     */
    suspend fun read(): Keyset?

    /** Атомарно заменяет набор целиком; учёт попыток PIN не трогает. */
    suspend fun write(keyset: Keyset)

    /** Уровень защиты для UI; `null` — набор ещё не создан. */
    fun observeLevel(): Flow<KeyLevel?>

    /** Есть ли в наборе биометрическая копия ключа. */
    fun observeBiometric(): Flow<Boolean>

    /** Учёт неверных PIN; пустой, если ошибок не было. */
    suspend fun readAttempts(): PinAttempts

    /** Сохраняет учёт неверных PIN. */
    suspend fun writeAttempts(attempts: PinAttempts)

    /** Стирает всё: набор и учёт попыток. */
    suspend fun clear()
}
