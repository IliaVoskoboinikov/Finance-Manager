package soft.divan.financemanager.uikit.components

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

class DatePickerDatesTest {

    private lateinit var originalZone: TimeZone

    @Before
    fun setUp() {
        originalZone = TimeZone.getDefault()
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun `date maps to UTC midnight millis`() {
        assertThat(LocalDate.of(1970, 1, 2).toDatePickerMillis()).isEqualTo(MILLIS_PER_DAY)
    }

    @Test
    fun `UTC midnight millis map back to the same day`() {
        assertThat(datePickerMillisToLocalDate(MILLIS_PER_DAY)).isEqualTo(LocalDate.of(1970, 1, 2))
    }

    @Test
    fun `selected day does not shift in any device time zone`() {
        val date = LocalDate.of(2026, 10, 10)

        ZONES.forEach { zone ->
            TimeZone.setDefault(TimeZone.getTimeZone(zone))

            assertThat(datePickerMillisToLocalDate(date.toDatePickerMillis()))
                .describedAs(zone)
                .isEqualTo(date)
        }
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
        val ZONES = listOf(
            "America/New_York",
            "Pacific/Pago_Pago",
            "UTC",
            "Asia/Tokyo",
            "Pacific/Kiritimati"
        )
    }
}
