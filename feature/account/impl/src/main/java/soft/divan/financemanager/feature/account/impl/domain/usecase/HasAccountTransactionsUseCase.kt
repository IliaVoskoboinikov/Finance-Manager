package soft.divan.financemanager.feature.account.impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

/**
 * Проверяет, есть ли у счёта хотя бы одна операция.
 *
 * По этому флагу экран счёта выбирает предупреждение при удалении: счёт с операциями будет
 * заархивирован (скрыт, но операции сохранятся в истории), пустой — удалён физически.
 */
interface HasAccountTransactionsUseCase {
    suspend operator fun invoke(accountId: String): DomainResult<Boolean>
}
