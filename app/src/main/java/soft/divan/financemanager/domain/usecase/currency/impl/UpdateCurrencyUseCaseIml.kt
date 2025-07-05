package soft.divan.financemanager.domain.usecase.currency.impl

import soft.divan.financemanager.domain.model.CurrencyCode
import soft.divan.financemanager.domain.repository.CurrencyRepository
import soft.divan.financemanager.domain.usecase.currency.UpdateCurrencyUseCase
import javax.inject.Inject

class UpdateCurrencyUseCaseIml @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : UpdateCurrencyUseCase {

    override suspend fun invoke(currency: CurrencyCode) {
        currencyRepository.updateCurrency(currency)
    }
}