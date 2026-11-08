package soft.divan.financemanager.core.domain.utli

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class UiDateFormatterTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    @Test
    fun `formatDate formats LocalDate with dd_MM_yyyy pattern`() {
        val date = LocalDate.of(2024, 3, 7)

        assertThat(UiDateFormatter.formatDate(date)).isEqualTo("07.03.2024")
    }

    @Test
    fun `formatDate of Instant uses system zone`() {
        val instant = LocalDateTime.of(2024, 3, 7, 12, 30)
            .atZone(zone)
            .toInstant()

        assertThat(UiDateFormatter.formatDate(instant)).isEqualTo("07.03.2024")
    }

    @Test
    fun `formatTime of Instant uses system zone`() {
        val instant = LocalDateTime.of(2024, 3, 7, 23, 59)
            .atZone(zone)
            .toInstant()

        assertThat(UiDateFormatter.formatTime(instant)).isEqualTo("23:59")
    }

    @Test
    fun `formatDateTime of Instant uses system zone`() {
        val instant = LocalDateTime.of(2024, 3, 7, 14, 15)
            .atZone(zone)
            .toInstant()

        assertThat(UiDateFormatter.formatDateTime(instant)).isEqualTo("07.03.2024 14:15")
    }

    @Test
    fun `formateOfDay returns start of day instant in system zone`() {
        val date = LocalDate.of(2024, 6, 1)

        val expected = date.atStartOfDay(zone).toInstant()

        assertThat(UiDateFormatter.formateOfDay(date)).isEqualTo(expected)
    }

    @Test
    fun `toLocalDateTime converts instant in system zone`() {
        val local = LocalDateTime.of(2024, 3, 7, 12, 34, 56)

        assertThat(UiDateFormatter.toLocalDateTime(local.atZone(zone).toInstant())).isEqualTo(local)
    }

    @Test
    fun `withDate changes the day and keeps time of day with seconds`() {
        val instant = LocalDateTime.of(2024, 3, 7, 12, 34, 56, 789_000_000)
            .atZone(zone)
            .toInstant()

        val moved = UiDateFormatter.withDate(instant, LocalDate.of(2024, 5, 1))

        assertThat(moved).isEqualTo(
            LocalDateTime.of(2024, 5, 1, 12, 34, 56, 789_000_000).atZone(zone).toInstant()
        )
    }

    @Test
    fun `withTime changes time of day and keeps the date`() {
        val instant = LocalDateTime.of(2024, 3, 7, 12, 34, 56).atZone(zone).toInstant()

        val moved = UiDateFormatter.withTime(instant, LocalTime.of(8, 15))

        assertThat(moved).isEqualTo(LocalDateTime.of(2024, 3, 7, 8, 15).atZone(zone).toInstant())
    }
}
