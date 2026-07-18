package soft.divan.financemanager.feature.transaction.impl.precenter.model

data class AccountUi(
    val id: String,
    val name: String,
    val balance: String,
    val currencyId: String,
    /**
     * Архивный («призрачный») счёт. Такой счёт нельзя выбрать для операции (он исключён из пикера),
     * но отображается при редактировании старой операции, которая на него ссылается.
     */
    val archived: Boolean = false
)
