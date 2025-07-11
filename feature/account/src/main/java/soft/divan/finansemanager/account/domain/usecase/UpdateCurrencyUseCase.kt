package soft.divan.finansemanager.account.domain.usecase

import soft.divan.financemanager.core.domain.model.CurrencyCode

interface UpdateCurrencyUseCase {
    suspend operator fun invoke(currency: CurrencyCode)
}