package soft.divan.financemanager.core.domain.model

/**
 * Статус счёта — зеркалит серверный enum `EStatus` (сериализуется именами: `Active`/`Hidden`/`Deleted`).
 *
 * - [Active] — виден и доступен для операций.
 * - [Hidden] — скрыт из списков, но доступен для операций (зарезервировано, пока не используется).
 * - [Deleted] — «архивный»: скрыт из списков и пикера, но остаётся для истории операций. Возникает,
 *   когда пользователь удаляет счёт, на котором есть операции (физически такой счёт не удаляется).
 */
enum class AccountStatus {
    Active,
    Hidden,
    Deleted;

    companion object {
        /**
         * Парсит серверную/локальную строку статуса. Неизвестное или пустое значение (дрейф enum,
         * legacy-строки) трактуется как [Active] — чтобы не скрыть счёт из-за непонятного статуса.
         */
        fun fromWire(raw: String): AccountStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: Active
    }
}
