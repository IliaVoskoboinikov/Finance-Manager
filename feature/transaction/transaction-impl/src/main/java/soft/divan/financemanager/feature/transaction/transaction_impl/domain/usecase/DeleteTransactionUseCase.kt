package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

interface DeleteTransactionUseCase {
    suspend operator fun invoke(transactionId: Int): Result<Unit>
}