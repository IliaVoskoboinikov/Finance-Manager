package soft.divan.financemanager.core.database.seed

import soft.divan.financemanager.core.database.entity.CategoryEntity

/**
 * Справочник категорий, с которым создаётся база.
 *
 * Идентификаторы совпадают с серверным засевом (`DbInitializer` бекенда), поэтому при синке
 * залогиненного пользователя сервер обновляет **эти же строки** (`REPLACE` по id), а не
 * добавляет рядом дубли. Гость, у которого сети нет вовсе, получает полный справочник сразу.
 *
 * Раньше справочник поставлялся ассетом `category_db.db` через `createFromAsset`, но фактически
 * не доезжал: версия ассета (1) была ниже версии схемы, миграций не было, и Room выбрасывал его
 * деструктивным откатом — на свежей установке категорий было ноль. Базу, зашифрованную
 * SQLCipher, из открытого ассета не создать вовсе, поэтому засев перенесён в код.
 */
internal object DefaultCategories {

    /**
     * Отметка времени засева — начало эпохи: любая версия категории, пришедшая с сервера,
     * заведомо новее.
     */
    const val SEEDED_AT = "1970-01-01T00:00:00Z"

    val all: List<CategoryEntity> = listOf(
        category("8ec25ef1-92e9-4267-9f93-5cef7a206dc2", "Зарплата", "💰", isIncome = true),
        category("c228b14c-2060-4115-a3ce-1ac30e741234", "Фриланс", "💻", isIncome = true),
        category("089be429-4aa6-40a1-aab7-7fc763ff4932", "Подарки", "🎁", isIncome = true),
        category("962c2e56-d372-41fe-942c-b4e7b562bdc0", "Проценты по вкладам", "🏦", isIncome = true),
        category("68e198c6-4759-4051-ac48-7630e2e7db98", "Возврат долга", "🔄", isIncome = true),
        category("ce8a6ed9-6aa2-4a1b-8676-85d97466b43d", "Продажа имущества", "🏠", isIncome = true),
        category("794db1dc-c4cd-4836-85d1-71483c197db9", "Жильё", "🏠", isIncome = false),
        category("99bd7e92-b0b2-428f-b461-7400ecf509d8", "Продукты", "🍎", isIncome = false),
        category("e24f5cba-2fa9-4fa1-b4c5-b9140e42655c", "Транспорт", "🚗", isIncome = false),
        category("fe2e0257-7b19-4194-b412-8cd253d446ec", "Развлечения", "🎭", isIncome = false),
        category("909cc325-a4f6-44ef-9b55-7b7c3b60ef86", "Рестораны", "🍽️", isIncome = false),
        category("c283d146-b4c3-4d72-a8dc-6c835ccee030", "Одежда", "👕", isIncome = false),
        category("705c9b3e-e2de-459b-b78b-7089fd0ea915", "Здоровье", "🏥", isIncome = false),
        category("10f12527-5011-4237-9292-e86fbf334700", "Коммунальные услуги", "💡", isIncome = false),
        category("587cd29e-fb78-4bdb-818b-fa16fc018f1c", "Техника", "📱", isIncome = false),
        category("00ef1842-5d6c-4a47-a57a-39147de8af33", "Образование", "📚", isIncome = false),
        category("2aa08597-21ec-43f7-80f7-7da9acfda5b6", "Путешествия", "✈️", isIncome = false),
        category("df041b86-578e-4e0c-aa32-2c63e6015185", "Подписки", "📺", isIncome = false),
        category("fa640daa-cb85-43c4-89b7-8f518a8f8088", "Подарки", "🎀", isIncome = false),
        category("97cc434d-7c1e-4900-a134-3ef4845c4d8f", "Красота", "💄", isIncome = false),
        category("d9cdf036-2c80-449b-babf-48f9d8dc5726", "Спорт", "🏋️", isIncome = false),
        category("fa67619c-b8c9-4b87-ae03-0650b62ae219", "Домашние животные", "🐾", isIncome = false),
        category("cf22d158-fd05-4a8a-800d-8938a34319a9", "Хобби", "🎨", isIncome = false),
        category("8d571312-2530-472b-8c62-7329103b15dd", "Кредиты", "💳", isIncome = false)
    )

    private fun category(id: String, name: String, emoji: String, isIncome: Boolean) =
        CategoryEntity(
            id = id,
            createdAt = SEEDED_AT,
            updatedAt = SEEDED_AT,
            name = name,
            emoji = emoji,
            isIncome = isIncome
        )
}
