package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import java.security.GeneralSecurityException
import java.security.ProviderException
import javax.inject.Inject

class SavePinUseCaseImpl @Inject constructor(
    private val repository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SavePinUseCase {

    /**
     * PBKDF2 и шифрование KeyStore занимают сотни миллисекунд — только не на главном потоке.
     * Отказ KeyStore (`GeneralSecurityException`, на Android ещё и `ProviderException`) не роняет
     * приложение: PIN не сохранён, и пользователь может ввести его заново. Отмену корутины не
     * перехватываем — поэтому не `runCatching`.
     */
    override suspend fun invoke(pin: String): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            repository.savePin(pin)
            DomainResult.Success(Unit)
        } catch (e: GeneralSecurityException) {
            DomainResult.Failure(DomainError.Unknown(e))
        } catch (e: ProviderException) {
            DomainResult.Failure(DomainError.Unknown(e))
        }
    }
}
