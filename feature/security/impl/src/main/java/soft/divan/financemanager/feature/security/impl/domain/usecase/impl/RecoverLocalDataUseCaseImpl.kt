package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.RecoverLocalDataUseCase
import javax.inject.Inject

class RecoverLocalDataUseCaseImpl @Inject constructor(
    private val localData: LocalDataAccessRepository,
    private val securityRepository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecoverLocalDataUseCase {

    override suspend fun retry() = localData.initialize()

    /**
     * Данные под PIN стираются вместе с выходом из аккаунта — тогда снимается и PIN, как при
     * «забыл PIN»: он защищал уже стёртые данные, и замок перед экраном входа только мешал бы.
     * Порядок тот же: сначала данные, потом PIN.
     */
    override suspend fun recover() {
        val signedOut = localData.recover()
        if (signedOut) withContext(ioDispatcher) { securityRepository.deletePin() }
    }
}
