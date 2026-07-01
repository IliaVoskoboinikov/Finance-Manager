package soft.divan.financemanager.core.data.mapper

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeMapper {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun fromApi(value: String): Instant =
        OffsetDateTime.parse(value, formatter).toInstant()

    fun toApi(value: Instant): String =
        value.atOffset(ZoneOffset.UTC).format(formatter)

    /**
     * true, если метка [a] позже [b].
     *
     * Сравнивает распарсенные [Instant], а не строки: лексикографическое сравнение
     * ISO-8601 некорректно при разных смещениях/точности
     * (например, "...+03:00" < "...Z", хотя по UTC это более раннее время).
     *
     * При ошибке парсинга возвращает false — консервативно не считаем удалённую
     * версию новее и не перезаписываем локальную.
     */
    fun isAfter(a: String, b: String): Boolean =
        runCatching { fromApi(a).isAfter(fromApi(b)) }.getOrDefault(false)
}
