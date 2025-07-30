package soft.divan.financemanager.feature.security.security_impl.domain.repository

interface SecurityRepository {
    fun savePin(pin: String)
    fun getPin(): String?
    fun isPinSet(): Boolean
    fun deletePin()
}