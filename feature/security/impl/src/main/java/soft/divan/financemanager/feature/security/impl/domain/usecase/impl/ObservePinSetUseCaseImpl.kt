package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObservePinSetUseCase
import javax.inject.Inject

class ObservePinSetUseCaseImpl @Inject constructor(
    private val repository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ObservePinSetUseCase {

    // Первое чтение SharedPreferences идёт с диска — не на главном потоке
    override fun invoke(): Flow<Boolean> = repository.observePinSet().flowOn(ioDispatcher)
}
