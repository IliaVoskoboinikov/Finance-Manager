package soft.divan.financemanager.domain.utils


class Exzeption(
    val reason: Reazon? = Reazon.UNKNOWN_ERROR,
    val wrapped: Throwable? = null,
    val data: Any? = null
) : Exception(wrapped)
