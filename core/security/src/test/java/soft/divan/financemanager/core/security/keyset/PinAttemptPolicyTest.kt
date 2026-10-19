package soft.divan.financemanager.core.security.keyset

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class PinAttemptPolicyTest {

    /** Часы системы — их пользователь может перевести. */
    private var now = Instant.parse("2026-09-01T12:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant(): Instant = now
    }

    /** Монотонные часы — время с загрузки устройства. */
    private var elapsed = Duration.ofHours(DAY_HOURS)
    private val policy = PinAttemptPolicy(clock) { elapsed.toMillis() }

    /** Проходит реальное время: идут и часы системы, и монотонные. */
    private fun pass(duration: Duration) {
        now += duration
        elapsed += duration
    }

    @Test
    fun `first four failures carry no lockout`() {
        var attempts = PinAttempts()
        repeat(PinAttemptPolicy.FREE_FAILURES) {
            attempts = policy.afterFailure(attempts)
            assertThat(policy.lockedUntil(attempts)).isNull()
        }
        assertThat(attempts.failures).isEqualTo(4)
        assertThat(policy.attemptsLeft(attempts)).isEqualTo(6)
    }

    @Test
    fun `failures five to nine lock for 30s, 1m, 5m, 15m and 1h`() {
        var attempts = PinAttempts(failures = PinAttemptPolicy.FREE_FAILURES)
        val expected = listOf(
            Duration.ofSeconds(30),
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(15),
            Duration.ofHours(1)
        )

        expected.forEach { lockout ->
            attempts = policy.afterFailure(attempts)
            assertThat(policy.lockedUntil(attempts)).isEqualTo(now + lockout)
        }
        assertThat(attempts.failures).isEqualTo(9)
        assertThat(policy.isExhausted(attempts)).isFalse()
        assertThat(policy.attemptsLeft(attempts)).isEqualTo(1)
    }

    @Test
    fun `tenth failure exhausts the attempts`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 9))

        assertThat(policy.isExhausted(attempts)).isTrue()
        assertThat(policy.attemptsLeft(attempts)).isZero()
        // Ступеней после девятой нет — пауза не нужна, дальше только стирание
        assertThat(attempts.lockoutMillis).isZero()
        assertThat(policy.lockedUntil(attempts)).isNull()
    }

    @Test
    fun `lockout ends when its time has passed`() {
        val attempts = policy.afterFailure(PinAttempts(failures = PinAttemptPolicy.FREE_FAILURES))

        pass(Duration.ofSeconds(29))
        assertThat(policy.lockedUntil(attempts)).isEqualTo(now + Duration.ofSeconds(1))

        pass(Duration.ofSeconds(1))
        assertThat(policy.lockedUntil(attempts)).isNull()
    }

    @Test
    fun `moving the clock back does not lift or stretch the lockout`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 8))

        // Часы переведены на два часа назад, монотонные идут как шли
        now -= Duration.ofHours(2)

        assertThat(policy.lockedUntil(attempts)).isEqualTo(now + Duration.ofHours(1))
        pass(Duration.ofHours(1))
        assertThat(policy.lockedUntil(attempts)).isNull()
    }

    @Test
    fun `moving the clock forward does not lift the lockout`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 8))

        // Часы переведены на сутки вперёд — по старой схеме пауза снялась бы сразу
        now += Duration.ofDays(1)

        assertThat(policy.lockedUntil(attempts)).isEqualTo(now + Duration.ofHours(1))
    }

    @Test
    fun `reboot counts only the time since boot`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 8))

        // Перезагрузка: монотонные часы начались заново и показывают 10 минут с загрузки
        elapsed = Duration.ofMinutes(10)

        assertThat(policy.lockedUntil(attempts)).isEqualTo(now + Duration.ofMinutes(50))
        pass(Duration.ofMinutes(50))
        assertThat(policy.lockedUntil(attempts)).isNull()
    }

    @Test
    fun `reboot never makes the lockout longer than a full one`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 8))

        // Перезагрузка и сразу проверка: засчитать нечего, пауза — полная, но не больше
        elapsed = Duration.ZERO

        assertThat(policy.lockedUntil(attempts)).isEqualTo(now + Duration.ofHours(1))
    }

    @Test
    fun `failure is anchored to the monotonic clock`() {
        val attempts = policy.afterFailure(PinAttempts(failures = PinAttemptPolicy.FREE_FAILURES))

        assertThat(attempts.lockoutStartedAtElapsed).isEqualTo(elapsed.toMillis())
        assertThat(attempts.lockoutMillis).isEqualTo(Duration.ofSeconds(30).toMillis())
    }

    @Test
    fun `clean record is neither locked nor exhausted`() {
        assertThat(policy.lockedUntil(PinAttempts())).isNull()
        assertThat(policy.isExhausted(PinAttempts())).isFalse()
        assertThat(policy.attemptsLeft(PinAttempts())).isEqualTo(PinAttemptPolicy.MAX_FAILURES)
    }

    @Test
    fun `attempts left never goes negative`() {
        assertThat(policy.attemptsLeft(PinAttempts(failures = 42))).isZero()
    }

    private companion object {
        /** Устройство работает сутки — точка отсчёта монотонных часов далеко от нуля. */
        const val DAY_HOURS = 24L
    }
}
