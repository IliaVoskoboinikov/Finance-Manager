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
}
