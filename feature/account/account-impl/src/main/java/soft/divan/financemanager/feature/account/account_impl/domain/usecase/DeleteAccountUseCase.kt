package soft.divan.financemanager.feature.account.account_impl.domain.usecase

interface DeleteAccountUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}