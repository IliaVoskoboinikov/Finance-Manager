package soft.divan.financemanager.core.loggingerror.impl

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jakarta.inject.Inject
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger

class CrashlyticsLogger @Inject constructor() : ErrorLogger {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordError(message: String?) {
        Log.e("recordError", message.toString())
        crashlytics.recordException(Exception(message))
    }
}