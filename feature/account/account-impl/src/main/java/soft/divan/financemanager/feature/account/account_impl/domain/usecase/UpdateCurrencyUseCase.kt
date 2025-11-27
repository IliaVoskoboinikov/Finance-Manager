package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.CurrencySymbol

interface UpdateCurrencyUseCase {
    suspend operator fun invoke(currency: CurrencySymbol)
}