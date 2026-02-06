package soft.divan.financemanager.feature.myaccounts.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.feature.myaccounts.impl.domain.usecase.UpdateCurrencyUseCase
import javax.inject.Inject

class UpdateCurrencyUseCaseIml @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : UpdateCurrencyUseCase {

    override suspend fun invoke(currency: CurrencySymbol) {
        currencyRepository.update(currency)
    }
}
// Revue me>>
