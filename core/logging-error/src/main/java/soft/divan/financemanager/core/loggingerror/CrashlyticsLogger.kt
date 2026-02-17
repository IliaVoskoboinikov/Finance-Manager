package soft.divan.financemanager.core.loggingerror

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jakarta.inject.Inject

class CrashlyticsLogger @Inject constructor() : ErrorLogger {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordError(message: String?) {
        Log.e("recordError", message.toString())
        crashlytics.recordException(Exception(message))
    }
}
