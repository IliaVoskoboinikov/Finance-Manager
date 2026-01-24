package soft.divan.financemanager.core.domain.model

import java.time.LocalDate

data class Period(
    val startDate: LocalDate,
    val endDate: LocalDate
)