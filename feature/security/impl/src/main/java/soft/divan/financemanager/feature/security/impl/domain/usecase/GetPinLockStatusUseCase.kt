package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus

/** Сколько попыток PIN осталось и не запрещён ли ввод — чтобы показать это до первой ошибки. */
interface GetPinLockStatusUseCase {
    suspend operator fun invoke(): PinLockStatus
}
