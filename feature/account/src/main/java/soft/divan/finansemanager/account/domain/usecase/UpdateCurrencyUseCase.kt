package soft.divan.finansemanager.account.domain.usecase

import soft.divan.core.currency.CurrencyCode

interface UpdateCurrencyUseCase {
    suspend operator fun invoke(currency: CurrencyCode)
}