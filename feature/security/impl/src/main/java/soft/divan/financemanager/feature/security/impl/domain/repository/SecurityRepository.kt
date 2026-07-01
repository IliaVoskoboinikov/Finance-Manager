package soft.divan.financemanager.feature.security.impl.domain.repository

interface SecurityRepository {
    fun savePin(pin: String)
    fun verifyPin(pin: String): Boolean
    fun isPinSet(): Boolean
    fun deletePin()
}
