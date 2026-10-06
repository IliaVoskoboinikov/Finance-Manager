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
 * Перевод часов назад паузу не снимает: если текущее время раньше последней ошибки, ввод
 * остаётся заблокированным до записанного момента.
 */
class PinAttemptPolicy @Inject constructor(private val clock: Clock) {

    /** Момент, до которого ввод запрещён, или `null`, если вводить можно. */
    fun lockedUntil(attempts: PinAttempts): Instant? {
        if (attempts.lockedUntilMillis == 0L) return null
        val now = clock.millis()
        val clockMovedBack = now < attempts.lastFailureAtMillis
        return Instant.ofEpochMilli(attempts.lockedUntilMillis)
            .takeIf { clockMovedBack || now < attempts.lockedUntilMillis }
    }

    /** Лимит исчерпан — данные подлежат стиранию. */
    fun isExhausted(attempts: PinAttempts): Boolean = attempts.failures >= MAX_FAILURES

    /** Сколько попыток осталось до стирания. */
    fun attemptsLeft(attempts: PinAttempts): Int = (MAX_FAILURES - attempts.failures).coerceAtLeast(0)

    /** Учёт с ещё одной ошибкой: счётчик, пауза и отметка времени. */
    fun afterFailure(attempts: PinAttempts): PinAttempts {
        val now = clock.millis()
        val failures = attempts.failures + 1
        val lockout = LOCKOUTS.getOrNull(failures - FREE_FAILURES - 1)
        return PinAttempts(
            failures = failures,
            lockedUntilMillis = lockout?.let { now + it.toMillis() } ?: 0L,
            lastFailureAtMillis = now
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
