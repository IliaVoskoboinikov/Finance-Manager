package soft.divan.financemanager.feature.account.impl.precenter.model

import java.time.Instant

/**
 * Состояние формы счёта.
 *
 * Метки времени — [Instant], а не отформатированные строки: строка `dd.MM.yyyy HH:mm` теряет
 * секунды, и сохранённая из неё правка проигрывала бы last-write-wins серверной версии.
 */
data class AccountUi(
    val id: String,
    val name: String,
    val balance: String,
    val currencyId: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
