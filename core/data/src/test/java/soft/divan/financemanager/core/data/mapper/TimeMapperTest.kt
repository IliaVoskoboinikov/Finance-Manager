package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant

class TimeMapperTest {

    @Test
    fun `fromApi parses UTC timestamp`() {
        val instant = TimeMapper.fromApi("2024-01-01T12:00:00Z")

        assertThat(instant).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"))
    }

    @Test
    fun `fromApi normalizes non-zero offset to instant`() {
        val instant = TimeMapper.fromApi("2024-01-01T12:00:00+03:00")

        assertThat(instant).isEqualTo(Instant.parse("2024-01-01T09:00:00Z"))
    }

    @Test
    fun `toApi formats instant as UTC ISO offset date time`() {
        val formatted = TimeMapper.toApi(Instant.parse("2024-01-01T09:00:00Z"))

        assertThat(formatted).isEqualTo("2024-01-01T09:00:00Z")
    }

    @Test
    fun `toApi and fromApi round-trip`() {
        val instant = Instant.parse("2024-06-15T23:59:59.123Z")

        assertThat(TimeMapper.fromApi(TimeMapper.toApi(instant))).isEqualTo(instant)
    }

    @Test
    fun `isAfter returns true when first timestamp is later`() {
        assertThat(TimeMapper.isAfter("2024-01-02T00:00:00Z", "2024-01-01T00:00:00Z")).isTrue()
    }

    @Test
    fun `isAfter returns false when first timestamp is earlier`() {
        assertThat(TimeMapper.isAfter("2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z")).isFalse()
    }

    @Test
    fun `isAfter returns false for equal timestamps`() {
        assertThat(TimeMapper.isAfter("2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z")).isFalse()
    }

    @Test
    fun `isAfter compares instants not strings across offsets`() {
        // "12:00+03:00" == 09:00Z, поэтому 10:00Z позже, хотя лексикографически строка меньше
        assertThat(TimeMapper.isAfter("2024-01-01T10:00:00Z", "2024-01-01T12:00:00+03:00")).isTrue()
        assertThat(TimeMapper.isAfter("2024-01-01T12:00:00+03:00", "2024-01-01T10:00:00Z")).isFalse()
    }

    @Test
    fun `isAfter returns false when first timestamp is malformed`() {
        assertThat(TimeMapper.isAfter("not-a-date", "2024-01-01T00:00:00Z")).isFalse()
    }

    @Test
    fun `isAfter returns false when second timestamp is malformed`() {
        assertThat(TimeMapper.isAfter("2024-01-01T00:00:00Z", "not-a-date")).isFalse()
    }
}
