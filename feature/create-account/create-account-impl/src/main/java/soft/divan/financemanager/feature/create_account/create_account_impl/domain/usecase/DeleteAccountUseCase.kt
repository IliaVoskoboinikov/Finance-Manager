package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase

interface DeleteAccountUseCase {
    suspend operator fun invoke(id: Int): Result<Unit>
}