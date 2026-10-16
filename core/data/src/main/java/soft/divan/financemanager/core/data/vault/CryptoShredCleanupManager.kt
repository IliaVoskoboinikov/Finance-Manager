package soft.divan.financemanager.core.data.vault

import soft.divan.financemanager.core.database.util.DatabaseCleanupManager
import javax.inject.Inject

/**
 * Очистка пользовательских данных при выходе из аккаунта — крипто-стиранием.
 *
 * Раньше строки удалялись `DELETE`-ом, но SQLite оставляет их в свободных страницах файла, и с
 * тем же ключом они читаемы. Теперь уничтожаются ключ и файл, а следующий пользователь получает
 * новую базу с новым ключом.
 *
 * Живёт в `:core:data`, а не в `:core:database`: стирание — это операция с ключами, а выход из
 * аккаунта инициирует `:core:auth`, которому хранилище ключей недоступно напрямую.
 */
class CryptoShredCleanupManager @Inject constructor(
    private val vault: LocalDataVault
) : DatabaseCleanupManager {

    override suspend fun clearUserData() = vault.wipe()
}
