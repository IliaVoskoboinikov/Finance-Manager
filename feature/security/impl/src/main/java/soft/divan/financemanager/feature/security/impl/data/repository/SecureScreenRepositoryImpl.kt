package soft.divan.financemanager.feature.security.impl.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.security.impl.data.sourse.SecureScreenLocalDataSource
import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import javax.inject.Inject

class SecureScreenRepositoryImpl @Inject constructor(
    private val localDataSource: SecureScreenLocalDataSource
) : SecureScreenRepository {

    override fun observeEnabled(): Flow<Boolean> = localDataSource.observeEnabled()

    override suspend fun setEnabled(enabled: Boolean) = localDataSource.setEnabled(enabled)
}
