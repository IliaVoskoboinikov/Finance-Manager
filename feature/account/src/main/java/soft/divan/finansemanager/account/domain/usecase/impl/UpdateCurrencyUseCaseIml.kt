package soft.divan.finansemanager.account.domain.usecase.impl

import soft.divan.core.currency.CurrencyCode
import soft.divan.core.currency.repository.CurrencyRepository
import soft.divan.finansemanager.account.domain.usecase.UpdateCurrencyUseCase
import javax.inject.Inject

class UpdateCurrencyUseCaseIml @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : UpdateCurrencyUseCase {

    override suspend fun invoke(currency: CurrencyCode) {
        currencyRepository.updateCurrency(currency)
    }
}