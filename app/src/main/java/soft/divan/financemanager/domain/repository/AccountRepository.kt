package soft.divan.financemanager.domain.repository

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.utils.Rezult

interface AccountRepository {
    suspend fun getAccounts(): Rezult<List<Account>>
}