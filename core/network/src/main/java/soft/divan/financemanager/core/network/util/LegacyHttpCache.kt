package soft.divan.financemanager.core.network.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * HTTP-кеш OkHttp, который вели прежние версии приложения.
 *
 * Кеш выключен: ответы API с суммами лежали в `cacheDir` открытым текстом, а данные и так живут в
 * зашифрованной базе. Каталог, оставшийся от прежних версий, удаляется при старте.
 */
class LegacyHttpCache @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Удаляет каталог кеша; если его нет — ничего не делает. Дисковая операция. */
    fun delete() {
        File(context.cacheDir, DIRECTORY).deleteRecursively()
    }

    private companion object {
        const val DIRECTORY = "http_cache"
    }
}
