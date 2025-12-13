package soft.divan.financemanager.core.logging_error.logging_error_impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import jakarta.inject.Inject
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger

class CrashlyticsLogger @Inject constructor() : ErrorLogger {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordError(message: String?) {
        crashlytics.recordException(Exception(message))
    }
}