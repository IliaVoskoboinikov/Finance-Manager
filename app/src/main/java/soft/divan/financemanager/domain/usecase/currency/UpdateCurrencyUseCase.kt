package soft.divan.financemanager.domain.usecase.currency

import soft.divan.financemanager.domain.model.CurrencyCode

interface UpdateCurrencyUseCase {
    suspend operator fun invoke(currency: CurrencyCode)
}