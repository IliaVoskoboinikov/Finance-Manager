package soft.divan.financemanager.core.data.outbox

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.random.Random

/** Тесты [OutboxRetryPolicy]: рост пауз, их потолок, размытие и порог ухода в dead-letter. */
class OutboxRetryPolicyTest {

    private val policy = OutboxRetryPolicy(Random(seed = 42))

    @Test
    fun `delay grows exponentially between attempts`() {
        val first = policy.delayMillis(1)
        val second = policy.delayMillis(2)
        val third = policy.delayMillis(3)

        // Нижние границы equal jitter удваиваются: 15s, 30s, 60s
        assertThat(first).isBetween(15_000L, 30_000L)
        assertThat(second).isBetween(30_000L, 60_000L)
        assertThat(third).isBetween(60_000L, 120_000L)
    }

    @Test
    fun `delay is capped so retries never drift beyond an hour`() {
        repeat(20) {
            assertThat(policy.delayMillis(attemptCount = 30)).isBetween(1_800_000L, 3_600_000L)
        }
    }

    @Test
    fun `jitter spreads delays of concurrent clients`() {
        val delays = (1..50).map { policy.delayMillis(attemptCount = 5) }.toSet()

        // Одинаковая для всех пауза означала бы синхронный повтор всей армией клиентов
        assertThat(delays).hasSizeGreaterThan(1)
    }

    @Test
    fun `nextAttemptAt is the delay measured from now`() {
        val now = 1_700_000_000_000L

        val next = policy.nextAttemptAt(now = now, attemptCount = 1)

        assertThat(next - now).isBetween(15_000L, 30_000L)
    }

    @Test
    fun `attempts are exhausted only at the configured limit`() {
        assertThat(policy.isExhausted(OutboxRetryPolicy.MAX_ATTEMPTS - 1)).isFalse()
        assertThat(policy.isExhausted(OutboxRetryPolicy.MAX_ATTEMPTS)).isTrue()
    }

    @Test
    fun `first attempt never gets a negative or zero-shift delay`() {
        assertThat(policy.delayMillis(attemptCount = 0)).isPositive()
        assertThat(policy.delayMillis(attemptCount = 1)).isPositive()
    }
}
