package soft.divan.financemanager.lifecycle

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.core.domain.usecase.LockLocalDataUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Закрывает базу уровня PIN, когда приложение надолго ушло в фон.
 *
 * Интерфейс запирается сразу при уходе в фон (замок в `MainActivity`), а база — только через
 * [delay]: короткий выход в другое приложение не прерывает запись и отправку изменений. На
 * уровнях без PIN закрывать нечего — там сценарий ничего не делает.
 *
 * Таймер в фоне может не дойти до срока: Android замораживает кешированные процессы. Поэтому при
 * возвращении проверяется, сколько приложение провело в фоне, и просроченное закрытие выполняется
 * сразу — до того, как пользователь увидит данные.
 *
 * Наблюдатель регистрируется на `ProcessLifecycleOwner`: его `ON_STOP` приходит, когда в фоне
 * всё приложение, а не одна Activity.
 */
@Singleton
class LocalDataAutoLock internal constructor(
    private val lockLocalData: LockLocalDataUseCase,
    private val scope: CoroutineScope,
    private val delay: Duration,
    private val elapsedRealtime: () -> Long
) : DefaultLifecycleObserver {

    @Inject
    constructor(
        lockLocalData: LockLocalDataUseCase,
        @ApplicationScope scope: CoroutineScope
    ) : this(lockLocalData, scope, AUTO_LOCK_DELAY, SystemClock::elapsedRealtime)

    private var pending: Job? = null
    private var stoppedAt: Long? = null

    override fun onStop(owner: LifecycleOwner) {
        stoppedAt = elapsedRealtime()
        pending?.cancel()
        pending = scope.launch {
            delay(delay)
            lockLocalData()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        pending?.cancel()
        pending = null
        val awaySince = stoppedAt ?: return
        stoppedAt = null
        if (elapsedRealtime() - awaySince >= delay.inWholeMilliseconds) {
            scope.launch { lockLocalData() }
        }
    }

    companion object {
        /** Сколько приложение может пробыть в фоне, прежде чем база уровня PIN закроется. */
        val AUTO_LOCK_DELAY: Duration = 5.minutes
    }
}
