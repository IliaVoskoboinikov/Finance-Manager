package soft.divan.financemanager.core.auth.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import javax.inject.Inject

/**
 * Возвращает статус авторизации из стейт-машины ([AuthStateProvider]) — единого источника
 * истины. Раньше use case читал DataStore напрямую, из-за чего UI/навигация и сетевые
 * интерцепторы могли видеть рассинхронизированное состояние (запись идёт memory-first,
 * диск обновляется как побочный эффект). Теперь источник один — in-memory состояние.
 */
class GetAuthStatusUseCaseImpl @Inject constructor(
    private val authStateProvider: AuthStateProvider
) : GetAuthStatusUseCase {
    override fun invoke(): Flow<AuthStatus> {
        return authStateProvider.observeStatus()
    }
}
