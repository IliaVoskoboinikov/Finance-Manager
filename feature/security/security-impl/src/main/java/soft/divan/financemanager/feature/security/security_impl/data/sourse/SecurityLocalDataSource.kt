package soft.divan.financemanager.feature.security.security_impl.data.sourse

interface SecurityLocalDataSource {
    fun savePin(pin: String)
    fun getPin(): String?
    fun isPinSet(): Boolean
}