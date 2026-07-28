package soft.divan.financemanager.core.loggingerror

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jakarta.inject.Inject

class CrashlyticsLogger @Inject constructor() : ErrorLogger {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordError(throwable: Throwable, message: String?) {
        Log.e(TAG, message ?: throwable.message ?: throwable.toString(), throwable)
        // Отправляем оригинальное исключение: сохраняются стек места сбоя, тип и cause,
        // а Crashlytics группирует ошибки по типу и фрейму.
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }

    override fun recordError(message: String) {
        Log.e(TAG, message)
        // Логическая ошибка без исключения: заворачиваем в именованный тип, чтобы отличать
        // такие записи от реальных крашей в Crashlytics.
        crashlytics.recordException(LoggedError(message))
    }

    private class LoggedError(message: String) : Exception(message)

    private companion object {
        const val TAG = "ErrorLogger"
    }
}
