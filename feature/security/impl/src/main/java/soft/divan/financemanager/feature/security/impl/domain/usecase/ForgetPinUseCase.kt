package soft.divan.financemanager.feature.security.impl.domain.usecase

/**
 * «Забыл PIN»: стирает данные и ключи, выходит из аккаунта и удаляет PIN.
 *
 * Выход обязателен на любом уровне: иначе кнопка стала бы обходом замка — данные вернулись бы с
 * сервера по сохранённой сессии без всякого PIN.
 */
interface ForgetPinUseCase {
    suspend operator fun invoke()
}
