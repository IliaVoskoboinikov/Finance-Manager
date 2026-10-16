package soft.divan.financemanager.core.security.keyset

import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Сколько неверных PIN можно ввести на уровне [KeyLevel.PIN] и что за это бывает.
 *
 * Первые [FREE_FAILURES] ошибок — без штрафа (опечатки случаются). Дальше каждая ошибка
 * включает паузу из [LOCKOUTS], а [MAX_FAILURES]-я — стирание данных.
 *
 * ### Перевод часов
 * Пауза отсчитывается по [MonotonicClock], а не по часам системы, поэтому её не сокращает и не
 * продлевает перевод часов ни назад, ни вперёд. Часы системы ([clock]) нужны только, чтобы
 * сообщить экрану момент окончания паузы.
 *
 * Перезагрузка сбрасывает монотонные часы в ноль. Ошибка тогда была до загрузки, и с неё прошло
 * не меньше, чем с загрузки, — засчитывается только это время. Поэтому перезагрузка паузу не
 * сокращает, а продлить её может не больше чем на одну полную паузу.
 */
class PinAttemptPolicy @Inject constructor(
    private val clock: Clock,
    private val monotonic: MonotonicClock
) {

    /** Момент, до которого ввод запрещён, или `null`, если вводить можно. */
    fun lockedUntil(attempts: PinAttempts): Instant? =
        remaining(attempts)?.let { clock.instant() + it }

    /** Сколько ещё длится пауза; `null` — паузы нет или она закончилась. */
    private fun remaining(attempts: PinAttempts): Duration? {
        if (attempts.lockoutMillis <= 0L) return null
        val now = monotonic.elapsedMillis()
        val rebooted = now < attempts.lockoutStartedAtElapsed
        val served = if (rebooted) now else now - attempts.lockoutStartedAtElapsed
        val left = attempts.lockoutMillis - served
        return if (left > 0) Duration.ofMillis(left) else null
    }

    /** Лимит исчерпан — данные подлежат стиранию. */
    fun isExhausted(attempts: PinAttempts): Boolean = attempts.failures >= MAX_FAILURES

    /** Сколько попыток осталось до стирания. */
    fun attemptsLeft(attempts: PinAttempts): Int = (MAX_FAILURES - attempts.failures).coerceAtLeast(0)

    /** Учёт с ещё одной ошибкой: счётчик, пауза и её точка отсчёта на монотонных часах. */
    fun afterFailure(attempts: PinAttempts): PinAttempts {
        val failures = attempts.failures + 1
        val lockout = LOCKOUTS.getOrNull(failures - FREE_FAILURES - 1)
        return PinAttempts(
            failures = failures,
            lockoutMillis = lockout?.toMillis() ?: 0L,
            lockoutStartedAtElapsed = monotonic.elapsedMillis()
        )
    }

    companion object {
        /** Ошибки без паузы. */
        const val FREE_FAILURES = 4

        /** Ошибка с этим номером стирает данные. */
        const val MAX_FAILURES = 10

        /** Паузы после ошибок №5…№9. */
        val LOCKOUTS: List<Duration> = listOf(
            Duration.ofSeconds(30),
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(15),
            Duration.ofHours(1)
        )
    }
}
