package soft.divan.financemanager.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object DateHelper{

    private const val API_DATE_PATTERN = "yyyy-MM-dd"
    private const val API_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DISPLAY_DATE_PATTERN = "dd.MM.yyyy"
    private const val DISPLAY_DATETIME_PATTERN = "dd.MM.yyyy HH:mm"
    private const val DISPLAY_TIME_PATTERN = "HH:mm"

    private val apiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(API_DATE_PATTERN).withLocale(Locale.getDefault())
    private val apiDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(API_DATETIME_PATTERN).withLocale(Locale.getDefault()).withZone(ZoneOffset.UTC)
    private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN).withLocale(Locale.getDefault())
    private val displayDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATETIME_PATTERN).withLocale(Locale.getDefault())
    private val displayTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_TIME_PATTERN).withLocale(Locale.getDefault())

    /** Сегодняшняя дата в API-формате (yyyy-MM-dd) */
    fun getTodayApiFormat(): String {
        return LocalDate.now().format(apiDateFormatter)
    }

    /** Первое число текущего месяца в формате отображения (dd.MM.yyyy) */
    fun getCurrentMonthStartDisplayFormat(): String {
        return LocalDate.now().withDayOfMonth(1).format(displayDateFormatter)
    }

    /** Сегодняшняя дата в формате отображения (dd.MM.yyyy) */
    fun getTodayDisplayFormat(): String {
        return LocalDate.now().format(displayDateFormatter)
    }

    /** Преобразует LocalDate → строка в формате API */
    fun dateToApiFormat(date: LocalDate): String {
        return date.format(apiDateFormatter)
    }

    /** Парсинг строки из API-формата (yyyy-MM-dd) → LocalDate */
    fun parseApiDate(apiDateString: String): LocalDate {
        return try {
            LocalDate.parse(apiDateString, apiDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /** Парсинг строки из отображения (dd.MM.yyyy) → LocalDate */
    fun parseDisplayDate(displayDateString: String): LocalDate {
        return try {
            LocalDate.parse(displayDateString, displayDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /** Парсинг API-формата DateTime (в UTC) → Instant */
    fun parseApiDateTime(apiDateTimeString: String): Instant {
        return try {
            Instant.from(apiDateTimeFormatter.parse(apiDateTimeString))
        } catch (e: Exception) {
            Instant.now()
        }
    }

    /** Форматирование LocalDate → строка отображения (dd.MM.yyyy) */
    fun formatDateForDisplay(date: LocalDate): String {
        return date.format(displayDateFormatter)
    }

    /** Форматирование LocalDateTime → строка отображения (dd.MM.yyyy HH:mm) */
    fun formatDateTimeForDisplay(dateTime: LocalDateTime): String {
        return dateTime.format(displayDateTimeFormatter)
    }

    /** Форматирование LocalTime → строка отображения (HH:mm) */
    fun formatTimeForDisplay(time: LocalTime): String {
        return time.format(displayTimeFormatter)
    }

    /**  Date → LocalDate */
    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /** LocalDate → Date */
    fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}
