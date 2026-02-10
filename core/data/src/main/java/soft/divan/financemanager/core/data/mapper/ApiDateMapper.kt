package soft.divan.financemanager.core.data.mapper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ApiDateMapper {

    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toApiDate(value: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        value
            .atZone(zoneId)
            .toLocalDate()
            .format(apiDateFormatter)
}
// Revue me>>
