package soft.divan.financemanager.core.database.model

/**
 * Операция, которую нужно отправить на сервер.
 *
 * Определяет HTTP-метод при отправке снимка (`payload`) и то, как трактовать ответ:
 * для [CREATE] «уже существует» — идемпотентный успех, для [DELETE] таким успехом является 404.
 */
enum class OutboxOperation {
    CREATE,
    UPDATE,
    DELETE
}
