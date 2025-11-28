package soft.divan.financemanager.core.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Вспомогательный объект для работы с датами и временем.
 *
 * Предоставляет методы форматирования, парсинга и преобразования между различными представлениями дат:
 * - **API-форматы**: `yyyy-MM-dd`, `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` (UTC)
 * - **Форматы отображения (display)**: `dd.MM.yyyy`, `dd.MM.yyyy HH:mm`, `HH:mm`
 *
 * Также содержит методы для получения текущей даты, начала месяца, и преобразования между
 * [java.util.Date], [java.time.LocalDate], [java.time.LocalDateTime], [java.time.Instant].
 *
 * Используется во всех слоях приложения: при работе с API, отображении дат на UI, фильтрации транзакций по времени и др.
 *
 * ### Основные шаблоны:
 * - `API_DATE_PATTERN`: формат даты для API: `yyyy-MM-dd`
 * - `API_DATETIME_PATTERN`: формат даты-времени для API (UTC): `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
 * - `DISPLAY_DATE_PATTERN`: формат отображения даты: `dd.MM.yyyy`
 * - `DISPLAY_DATETIME_PATTERN`: формат отображения даты и времени: `dd.MM.yyyy HH:mm`
 * - `DISPLAY_TIME_PATTERN`: формат отображения времени: `HH:mm`
 */

object DateHelper {

    private const val API_DATE_PATTERN = "yyyy-MM-dd"
    private const val API_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DISPLAY_DATE_PATTERN = "dd.MM.yyyy"
    private const val DISPLAY_DATETIME_PATTERN = "dd.MM.yyyy HH:mm"
    private const val DISPLAY_TIME_PATTERN = "HH:mm"

    private val apiDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(API_DATE_PATTERN).withLocale(Locale.getDefault())
    private val apiDateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(API_DATETIME_PATTERN).withLocale(Locale.getDefault())
            .withZone(ZoneOffset.UTC)
    private val displayDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN).withLocale(Locale.getDefault())
    private val displayDateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DISPLAY_DATETIME_PATTERN).withLocale(Locale.getDefault())
    private val displayTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DISPLAY_TIME_PATTERN).withLocale(Locale.getDefault())

    /**
     * Возвращает сегодняшнюю дату в формате, совместимом с API (`yyyy-MM-dd`).
     *
     * @return строка с сегодняшней датой
     */
    fun getTodayApiFormat(): String {
        return LocalDate.now().format(apiDateFormatter)
    }

    /**
     * Возвращает строку с первым числом текущего месяца в формате отображения (`dd.MM.yyyy`).
     */
    fun getCurrentMonthStartDisplayFormat(): String {
        return LocalDate.now().withDayOfMonth(1).format(displayDateFormatter)
    }

    /**
     * Возвращает сегодняшнюю дату в формате отображения (`dd.MM.yyyy`).
     */
    fun getTodayDisplayFormat(): String {
        return LocalDate.now().format(displayDateFormatter)
    }

    /**
     * Преобразует [LocalDate] в строку формата API (`yyyy-MM-dd`).
     *
     * @param date дата для форматирования
     * @return строка в формате API
     */
    fun dateToApiFormat(date: LocalDate): String {
        return date.format(apiDateFormatter)
    }

    /**
     * Парсит строку в формате API (`yyyy-MM-dd`) в [LocalDate].
     *
     * @param apiDateString строка, полученная из API
     * @return соответствующий [LocalDate], либо текущая дата при ошибке
     */
    fun parseApiDate(apiDateString: String): LocalDate {
        return try {
            LocalDate.parse(apiDateString, apiDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /**
     * Парсит строку из отображаемого формата (`dd.MM.yyyy`) в [LocalDate].
     *
     * @param displayDateString дата в виде строки
     * @return [LocalDate], либо текущая дата при ошибке парсинга
     */
    fun parseDisplayDate(displayDateString: String): LocalDate {
        return try {
            LocalDate.parse(displayDateString, displayDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun parseDisplayDateTime(displayDateTimeString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(displayDateTimeString, displayDateTimeFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    /**
     * Парсит строку из API-даты с временем в формате UTC в [java.time.Instant].
     *
     * @param apiDateTimeString строка формата `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
     * @return [java.time.Instant], либо текущий момент времени при ошибке
     */
    fun parseApiDateTime(apiDateTimeString: String): Instant {
        return try {
            Instant.from(apiDateTimeFormatter.parse(apiDateTimeString))
        } catch (e: Exception) {
            Instant.now()
        }
    }

    /**
     * Форматирует [LocalDate] в строку для отображения (`dd.MM.yyyy`).
     */
    fun formatDateForDisplay(date: LocalDate): String {
        return date.format(displayDateFormatter)
    }

    /**
     * Форматирует [java.time.LocalDateTime] в строку для отображения (`dd.MM.yyyy HH:mm`).
     */
    fun formatDateTimeForDisplay(dateTime: LocalDateTime): String {
        return dateTime.format(displayDateTimeFormatter)
    }

    fun dataTimeForApi(dateTime: LocalDateTime): String {
        return dateTime.format(apiDateTimeFormatter)
    }
    /**
     * Форматирует [java.time.LocalTime] в строку (`HH:mm`).
     */
    fun formatTimeForDisplay(time: LocalTime): String {
        return time.format(displayTimeFormatter)
    }

    fun formatTimeForDisplay(time: LocalDateTime): String {
        return time.format(displayTimeFormatter)
    }

    /**
     * Расширение для преобразования [java.util.Date] в [LocalDate].
     *
     * @return локализованная дата без времени
     */
    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /**
     * Расширение для преобразования [LocalDate] в [Date].
     *
     * @return дата, представляющая начало дня в системной зоне
     */
    fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}