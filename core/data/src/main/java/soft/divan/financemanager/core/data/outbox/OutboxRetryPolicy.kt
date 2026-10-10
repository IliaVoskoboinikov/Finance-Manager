package soft.divan.financemanager.core.data.outbox

import javax.inject.Inject
import kotlin.random.Random

/**
 * Политика повторов для записей очереди: когда пробовать снова и когда сдаться.
 *
 * Пауза растёт экспоненциально и размывается «equal jitter» — половина фиксирована, половина
 * случайна. Без размытия все устройства, столкнувшиеся с одним сбоем сервера, повторяли бы
 * синхронно и добивали бы его пачкой (thundering herd).
 *
 * Паузы здесь заметно длиннее транспортных ретраев `RetryInterceptor`: тот повторяет запрос
 * в пределах одного вызова (секунды), а очередь живёт между прогонами фоновой синхронизации,
 * и торопиться ей некуда — доставка всё равно «рано или поздно».
 */
class OutboxRetryPolicy(private val random: Random) {

    @Inject
    constructor() : this(Random.Default)

    /**
     * Попытки исчерпаны — запись пора уводить в dead-letter.
     *
     * Бесконечные повторы недопустимы: «отравленная» запись жгла бы батарею и держала бы очередь
     * (порядок строгий), а пользователь так и не узнал бы о проблеме.
     */
    fun isExhausted(attemptCount: Int): Boolean = attemptCount >= MAX_ATTEMPTS

    /** Момент, раньше которого повторять запись не следует. */
    fun nextAttemptAt(now: Long, attemptCount: Int): Long = now + delayMillis(attemptCount)

    /**
     * Пауза перед попыткой номер [attemptCount]: экспоненциальный рост от [BASE_DELAY_MILLIS],
     * ограничение [MAX_DELAY_MILLIS] и equal jitter — итог в диапазоне `[capped/2, capped]`.
     */
    internal fun delayMillis(attemptCount: Int): Long {
        val exponent = (attemptCount - 1).coerceIn(0, MAX_BACKOFF_SHIFT)
        val capped = (BASE_DELAY_MILLIS shl exponent).coerceAtMost(MAX_DELAY_MILLIS)
        val half = capped / 2
        return half + random.nextLong(half + 1)
    }

    companion object {
        /** После стольких неудачных попыток запись уходит в dead-letter. */
        const val MAX_ATTEMPTS = 8

        private const val BASE_DELAY_MILLIS = 30_000L
        private const val MAX_DELAY_MILLIS = 3_600_000L
        private const val MAX_BACKOFF_SHIFT = 16
    }
}
