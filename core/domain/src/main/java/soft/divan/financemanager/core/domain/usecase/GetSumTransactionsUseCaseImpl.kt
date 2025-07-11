package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Use case для подсчета суммы по списку транзакций.
 *
 * Реализует бизнес-логику агрегации суммы всех переданных транзакций
 * через метод `sumOf`, основываясь на значении поля `amount` каждого элемента списка.
 *
 * Данный use case используется для отображения общей суммы расходов, доходов,
 * баланса и других агрегированных показателей.
 *
 * @see Transaction
 */
class GetSumTransactionsUseCaseImpl @Inject constructor() : GetSumTransactionsUseCase {
    override operator fun invoke(transactions: List<Transaction>): BigDecimal {
        return transactions.sumOf { it.amount }

    }
}