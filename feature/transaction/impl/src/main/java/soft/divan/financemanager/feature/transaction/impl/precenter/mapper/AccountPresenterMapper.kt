package soft.divan.financemanager.feature.transaction.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountStatus
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi

/**
 * `archived` (архивный «призрак») выводится из статуса счёта: [AccountStatus.Deleted] — счёт был
 * удалён при наличии операций. Такой счёт нельзя выбрать для новой операции, но он показывается при
 * редактировании старой, которая на него ссылается.
 */
fun Account.toUi(): AccountUi {
    return AccountUi(
        id = id,
        name = name,
        balance = balance.toString(),
        currencyId = currencyId,
        archived = status == AccountStatus.Deleted
    )
}
