package soft.divan.financemanager.feature.security.impl.data.repository

import soft.divan.financemanager.feature.security.impl.data.crypto.PinHasher
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import javax.inject.Inject

class SecurityRepositoryImpl @Inject constructor(
    private val securityLocalDataSource: SecurityLocalDataSource
) : SecurityRepository {

    override fun savePin(pin: String) {
        securityLocalDataSource.savePin(PinHasher.hash(pin))
    }

    override fun verifyPin(pin: String): Boolean {
        val storedHash = securityLocalDataSource.getPin() ?: return false
        return PinHasher.verify(pin, storedHash)
    }

    override fun isPinSet(): Boolean {
        return securityLocalDataSource.isPinSet()
    }

    override fun deletePin() {
        securityLocalDataSource.deletePin()
    }
}
