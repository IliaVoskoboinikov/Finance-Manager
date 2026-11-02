package soft.divan.financemanager.core.data.source.impl

/**
 * Сколько id уходит в один запрос `getBySyncIds`.
 *
 * Запрос подставляет список дважды (`serverId IN (:ids) OR localId IN (:ids)`), то есть тратит
 * по две bind-переменные на id. Системный SQLite на API 26–30 допускает не больше 999 переменных:
 * без разбиения pull счёта с историей от ~500 операций падал бы, а pull принимал бы сбой за
 * «локально ничего нет» и вставлял серверные записи дубликатами. 400 id — 800 переменных, с
 * запасом под лимит.
 */
internal const val SYNC_IDS_CHUNK_SIZE = 400

/** Выполняет [query] по частям не длиннее [SYNC_IDS_CHUNK_SIZE] и склеивает результат. */
internal suspend fun <T> List<String>.queryInChunks(
    query: suspend (List<String>) -> List<T>
): List<T> = chunked(SYNC_IDS_CHUNK_SIZE).flatMap { query(it) }
