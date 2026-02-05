package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.domain.error.DomainError

fun DataError.toDomainError(): DomainError =
    when (this) {
        is DataError.Network -> DomainError.NetworkUnavailable
        is DataError.Unauthorized -> DomainError.Unauthorized
        is DataError.NotFound -> DomainError.NoData
        is DataError.Server -> DomainError.Unknown(null)
        is DataError.LocalDb -> DomainError.Unknown(cause)
        is DataError.Unknown -> DomainError.Unknown(cause)
    }
