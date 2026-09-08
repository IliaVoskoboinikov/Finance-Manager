package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

/** Повторяет отправку изменений, отложенных после исчерпания автоматических попыток. */
interface RetryUnsentOperationsUseCase {
    suspend operator fun invoke(): DomainResult<Unit>
}
