package soft.divan.financemanager.feature.account.account_impl.domain.usecase

interface DeleteAccountUseCase {
    suspend operator fun invoke(id: Int): Result<Unit>
}