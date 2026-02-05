package soft.divan.financemanager.core.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import java.math.BigDecimal
import javax.inject.Inject

class GetSumTransactionsUseCaseImpl @Inject constructor() : GetSumTransactionsUseCase {
    override operator fun invoke(transactions: List<Transaction>): BigDecimal {
        return transactions.sumOf { it.amount }
    }
    // todo сделать пересчет с учетом валюты , сделать выбор дефолтной валюты
}
