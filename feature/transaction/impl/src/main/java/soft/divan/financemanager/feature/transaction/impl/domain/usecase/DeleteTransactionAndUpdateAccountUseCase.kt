package soft.divan.financemanager.feature.transaction.impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

/**
 * Удаляет операцию и откатывает её сумму на счёте.
 *
 * Принимает только id: сумма, тип и счёт для отката берутся из сохранённой версии в БД, поэтому
 * несохранённые правки формы на удаление повлиять не могут.
 */
interface DeleteTransactionAndUpdateAccountUseCase {
    /**
     * Удаляет операцию [transactionId] и откатывает её сохранённую сумму на её счёте.
     *
     * @return [DomainResult.Failure], если операции или её счёта нет; тогда ничего не меняется.
     */
    suspend operator fun invoke(transactionId: String): DomainResult<Unit>
}
