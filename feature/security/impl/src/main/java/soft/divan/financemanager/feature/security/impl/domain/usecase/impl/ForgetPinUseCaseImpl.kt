package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ForgetPinUseCase
import javax.inject.Inject

class ForgetPinUseCaseImpl @Inject constructor(
    private val localData: LocalDataAccessRepository,
    private val securityRepository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ForgetPinUseCase {

    /**
     * Сначала данные, потом PIN: процесс, умерший посередине, оставит замок на уже пустом профиле
     * (повторное «забыл PIN» его снимет), а не снятый замок на ещё живых данных.
     */
    override suspend fun invoke() {
        localData.wipe(signOut = true)
        withContext(ioDispatcher) { securityRepository.deletePin() }
    }
}
