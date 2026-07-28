package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

/**
 * Возвращает счёт по его id.
 *
 * В отличие от [GetAccountsUseCase], не фильтрует архивные («призрачные») счета, поэтому нужен,
 * чтобы подтянуть имя/валюту архивного счёта старой операции (его нет в списке для выбора).
 */
interface GetAccountByIdUseCase {
    suspend operator fun invoke(id: String): DomainResult<Account>
}
