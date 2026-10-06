package soft.divan.financemanager.core.security.keyset

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class PinAttemptPolicyTest {

    private var now = Instant.parse("2026-09-01T12:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant(): Instant = now
    }
    private val policy = PinAttemptPolicy(clock)

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
        assertThat(attempts.lockedUntilMillis).isZero()
    }

    @Test
    fun `lockout ends when its time has passed`() {
        val attempts = policy.afterFailure(PinAttempts(failures = PinAttemptPolicy.FREE_FAILURES))

        now += Duration.ofSeconds(29)
        assertThat(policy.lockedUntil(attempts)).isNotNull()

        now += Duration.ofSeconds(1)
        assertThat(policy.lockedUntil(attempts)).isNull()
    }

    @Test
    fun `moving the clock back does not lift the lockout`() {
        val attempts = policy.afterFailure(PinAttempts(failures = 8))
        val lockedUntil = policy.lockedUntil(attempts)

        // Часы переведены на два часа назад: «сейчас» раньше последней ошибки
        now -= Duration.ofHours(2)

        assertThat(policy.lockedUntil(attempts)).isEqualTo(lockedUntil)
    }

    @Test
    fun `failure is stamped with the current time`() {
        val attempts = policy.afterFailure(PinAttempts())

        assertThat(attempts.lastFailureAtMillis).isEqualTo(now.toEpochMilli())
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
}
