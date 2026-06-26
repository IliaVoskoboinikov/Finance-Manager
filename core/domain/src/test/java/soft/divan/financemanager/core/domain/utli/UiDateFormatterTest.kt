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
    fun `formatDateTime formats LocalDateTime with date and time`() {
        val dateTime = LocalDateTime.of(2024, 12, 31, 9, 5)

        assertThat(UiDateFormatter.formatDateTime(dateTime)).isEqualTo("31.12.2024 09:05")
    }

    @Test
    fun `formatTime formats LocalTime with HH_mm pattern`() {
        val time = LocalTime.of(8, 4)

        assertThat(UiDateFormatter.formatTime(time)).isEqualTo("08:04")
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
    fun `parse of date-time string round-trips with formatDateTime`() {
        val instant = LocalDateTime.of(2024, 3, 7, 14, 15)
            .atZone(zone)
            .toInstant()

        val formatted = UiDateFormatter.formatDateTime(instant)
        val parsed = UiDateFormatter.parse(formatted)

        assertThat(parsed).isEqualTo(instant)
    }

    @Test
    fun `parse of separate date and time strings produces expected instant`() {
        val expected = LocalDateTime.of(2024, 3, 7, 14, 15)
            .atZone(zone)
            .toInstant()

        val parsed = UiDateFormatter.parse("07.03.2024", "14:15")

        assertThat(parsed).isEqualTo(expected)
    }
}
