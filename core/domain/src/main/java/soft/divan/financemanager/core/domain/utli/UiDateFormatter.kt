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

/**
 * Форматирование дат для показа в зоне и локали устройства.
 *
 * Работает только в одну сторону: обратного разбора строк нет намеренно. Строки обрезаны до
 * минут, и момент, собранный из них, терял бы секунды (FM-146) — UI-модели хранят [Instant],
 * а этот объект лишь отрисовывает его и меняет части момента ([withDate], [withTime]).
 */
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

    fun formateOfDay(
        date: LocalDate
    ): Instant =
        date.atStartOfDay(zoneId).toInstant()

    /* ---------- Части момента ---------- */

    /** Момент [instant] в зоне устройства — начальные значения пикеров даты и времени. */
    fun toLocalDateTime(instant: Instant): LocalDateTime =
        instant.atZone(zoneId).toLocalDateTime()

    /**
     * Переносит момент [instant] на дату [date], сохраняя время суток вместе с секундами.
     *
     * Пикер даты меняет только дату: если бы время собиралось из строки `HH:mm`, смена дня
     * заодно обнуляла бы секунды операции.
     */
    fun withDate(instant: Instant, date: LocalDate): Instant =
        instant.atZone(zoneId).with(date).toInstant()

    /**
     * Ставит моменту [instant] время суток [time], сохраняя дату в зоне устройства.
     *
     * Если время попадает в разрыв перехода на летнее время, оно сдвигается вперёд на длину
     * разрыва (`ZonedDateTime.with`). То же происходит и в [withDate].
     */
    fun withTime(instant: Instant, time: LocalTime): Instant =
        instant.atZone(zoneId).with(time).toInstant()
}
