package soft.divan.financemanager.domain.usecase.account

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.utils.Rezult

interface CreateAccountUseCase {
    suspend operator fun invoke(createAccountRequest: CreateAccountRequest): Rezult<Account>
}
