package soft.divan.financemanager.feature.security.impl.data.sourse

interface SecurityLocalDataSource {
    fun savePin(pin: String)
    fun getPin(): String?
    fun isPinSet(): Boolean
    fun deletePin()
}
