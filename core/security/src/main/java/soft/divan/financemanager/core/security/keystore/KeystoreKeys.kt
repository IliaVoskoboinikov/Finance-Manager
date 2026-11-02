package soft.divan.financemanager.core.security.keystore

import javax.crypto.SecretKey

/**
 * Ключи Android Keystore, которыми заворачивается ключ базы.
 *
 * В отличие от [soft.divan.financemanager.core.security.CryptoManager], здесь ключ **никогда не
 * пересоздаётся молча**. Для токенов потеря ключа означает лишь повторный вход, а для ключа базы —
 * безвозвратную потерю данных, поэтому отсутствующий или испорченный ключ — это явная ошибка,
 * о которой решает вызывающий код.
 *
 * Каждое заворачивание получает **новый алиас**, а старый удаляется только после того, как новое
 * состояние сохранено. Так смена уровня защиты не может уничтожить ключ, на который ещё ссылается
 * сохранённый набор.
 */
interface KeystoreKeys {

    /** Существующий ключ или `null`, если алиаса нет. Бросает [KeyUnavailableException]. */
    fun find(alias: String): SecretKey?

    /** Создаёт ключ под новым алиасом с префиксом [prefix]; алиас возвращается вместе с ключом. */
    fun create(prefix: String, protection: KeyProtection): AliasedKey

    fun delete(alias: String)

    /** Все алиасы с префиксом [prefix] — для уборки ключей, оставшихся после сбоя. */
    fun aliases(prefix: String): Set<String>
}

/** Ключ Keystore вместе с алиасом, под которым он создан. */
class AliasedKey(val alias: String, val key: SecretKey)
