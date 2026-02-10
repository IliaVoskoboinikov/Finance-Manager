package soft.divan.financemanager.core.domain.utli

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object UiDatePatterns {
    const val DISPLAY_DATE = "dd.MM.yyyy"
    const val DISPLAY_TIME = "HH:mm"
    const val DISPLAY_DATE_TIME = "dd.MM.yyyy HH:mm"
}

object UiDateFormatter {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val locale: Locale = Locale.getDefault()

    private val dateFormatter =
        DateTimeFormatter.ofPattern(UiDatePatterns.DISPLAY_DATE, locale)

    private val timeFormatter =
        DateTimeFormatter.ofPattern(UiDatePatterns.DISPLAY_TIME, locale)

    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern(UiDatePatterns.DISPLAY_DATE_TIME, locale)

    /* ---------- Instant ---------- */

    fun formatDate(instant: Instant): String =
        instant.atZone(zoneId).toLocalDate().format(dateFormatter)

    fun formatTime(instant: Instant): String =
        instant.atZone(zoneId).toLocalTime().format(timeFormatter)

    fun formatDateTime(instant: Instant): String =
        instant.atZone(zoneId).toLocalDateTime().format(dateTimeFormatter)

    /* ---------- LocalDate ---------- */

    fun formatDate(date: LocalDate): String =
        date.format(dateFormatter)

    /* ---------- LocalDateTime ---------- */

    fun formatDateTime(dateTime: LocalDateTime): String =
        dateTime.format(dateTimeFormatter)

    fun formatTime(dateTime: LocalTime): String =
        dateTime.format(timeFormatter)

    fun formateOfDay(
        date: LocalDate
    ): Instant =
        date.atStartOfDay(zoneId).toInstant()

    /* ---------- Parse ---------- */

    fun parse(dateTime: String): Instant =
        LocalDateTime
            .parse(dateTime, dateTimeFormatter)
            .atZone(zoneId)
            .toInstant()

    fun parse(date: String, time: String): Instant =
        parse("$date $time")
}
// Revue me>>
