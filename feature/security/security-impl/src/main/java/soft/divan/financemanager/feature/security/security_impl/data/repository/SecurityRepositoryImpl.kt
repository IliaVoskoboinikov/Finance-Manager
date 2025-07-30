package soft.divan.financemanager.feature.security.security_impl.data.repository

import soft.divan.financemanager.feature.security.security_impl.data.sourse.SecurityLocalDataSource
import soft.divan.financemanager.feature.security.security_impl.domain.repository.SecurityRepository
import javax.inject.Inject

class SecurityRepositoryImpl @Inject constructor(
    private val securityLocalDataSource: SecurityLocalDataSource
) : SecurityRepository {


    override fun savePin(pin: String) {
        securityLocalDataSource.savePin(pin)
    }

    override fun getPin(): String? {
        return securityLocalDataSource.getPin()
    }

    override fun isPinSet(): Boolean {
        return securityLocalDataSource.isPinSet()
    }

    override fun deletePin() {
        securityLocalDataSource.deletePin()
    }
}