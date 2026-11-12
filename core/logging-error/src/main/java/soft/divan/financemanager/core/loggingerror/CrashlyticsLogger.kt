package soft.divan.financemanager.core.loggingerror

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import soft.divan.financemanager.core.logging_error.BuildConfig

/**
 * [ErrorLogger] поверх Firebase Crashlytics: каждая ошибка идёт в logcat и в Crashlytics.
 *
 * В Crashlytics исключение уходит без текста ([redacted]): только тип и стек каждого звена
 * `cause`. Текст пишут библиотеки, и в него попадают разбираемые значения (дата, число из ответа).
 * В logcat release-сборки идёт та же копия, полный текст — только в debug. Тип в консоли
 * Crashlytics у всех таких записей — `RedactedException`; исходный — в её тексте и в ключе
 * события `original_type`.
 *
 * Фатальные краши сюда не попадают: их отправляет собственный обработчик Crashlytics, с текстом.
 *
 * Если `FirebaseApp` не поднят (сборка без `google-services.json`: свежий клон, PR из форка),
 * ошибки пишутся только в logcat — приложение работает и без Firebase.
 */
class CrashlyticsLogger internal constructor(
    private val context: Context,
    // Полный текст исключения в logcat; отдельным параметром — чтобы проверить обе ветки
    private val logFullText: Boolean
) : ErrorLogger {

    /** Конструктор для Hilt: полный текст исключений в logcat — только в debug-сборке. */
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, BuildConfig.DEBUG)

    // Лениво: FirebaseCrashlytics.getInstance() без FirebaseApp бросает IllegalStateException,
    // а проверка при сборке Hilt-графа не нужна — только при первой ошибке.
    private val crashlytics: FirebaseCrashlytics? by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseCrashlytics.getInstance()
    }

    override fun recordError(throwable: Throwable, message: String?) {
        if (logFullText) Log.e(TAG, message ?: throwable.message ?: throwable.toString(), throwable)
        // Логгер стоит на пути ошибки и сам бросать не должен: чужой Throwable может переопределить
        // getCause/getStackTrace. Тогда — копия одного верхнего звена, всё так же без текста.
        val redacted = runCatching { throwable.redacted() }
            .getOrElse { RedactedException(throwable.javaClass.name, cause = null) }
        if (!logFullText) Log.e(TAG, message ?: redacted.message.orEmpty(), redacted)
        val crashlytics = crashlytics ?: return
        message?.let { crashlytics.log(it) }
        // Тип записи в консоли у всех копий — RedactedException; исходный — ключом события,
        // чтобы по нему фильтровать
        val keys = CustomKeysAndValues.Builder()
            .putString(ORIGINAL_TYPE_KEY, throwable.javaClass.name)
            .build()
        crashlytics.recordException(redacted, keys)
    }

    override fun recordError(message: String) {
        Log.e(TAG, message)
        // Логическая ошибка без исключения: заворачиваем в именованный тип, чтобы отличать
        // такие записи от реальных крашей в Crashlytics.
        crashlytics?.recordException(LoggedError(message))
    }

    private class LoggedError(message: String) : Exception(message)

    private companion object {
        const val TAG = "ErrorLogger"
        const val ORIGINAL_TYPE_KEY = "original_type"
    }
}
