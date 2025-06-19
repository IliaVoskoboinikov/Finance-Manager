package soft.divan.financemanager.domain.usecase.account

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.utils.Rezult

interface GetAccountsUseCase {
    suspend operator fun invoke(): Rezult<List<Account>>
}