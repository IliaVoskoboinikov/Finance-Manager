package soft.divan.financemanager.core.loggingerror

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

/**
 * [ErrorLogger] поверх Firebase Crashlytics: каждая ошибка идёт в logcat и в Crashlytics.
 *
 * Если `FirebaseApp` не поднят (сборка без `google-services.json`: свежий клон, PR из форка),
 * ошибки пишутся только в logcat — приложение работает и без Firebase.
 */
class CrashlyticsLogger @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ErrorLogger {

    // Лениво: FirebaseCrashlytics.getInstance() без FirebaseApp бросает IllegalStateException,
    // а проверка при сборке Hilt-графа не нужна — только при первой ошибке.
    private val crashlytics: FirebaseCrashlytics? by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseCrashlytics.getInstance()
    }

    override fun recordError(throwable: Throwable, message: String?) {
        Log.e(TAG, message ?: throwable.message ?: throwable.toString(), throwable)
        val crashlytics = crashlytics ?: return
        // Отправляем оригинальное исключение: сохраняются стек места сбоя, тип и cause,
        // а Crashlytics группирует ошибки по типу и фрейму.
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
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
    }
}
