package soft.divan.financemanager.presenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Прошёл ли пользователь замок приложения (PIN или отпечаток) в этом процессе.
 *
 * Состояние живёт в ViewModel, а не в `rememberSaveable`: оно переживает пересоздание Activity
 * (смена темы, масштаба шрифта), но не смерть процесса. Сохранённое состояние Activity система
 * восстанавливает и в новом процессе — с ним замок пропускался бы после того, как приложение
 * выгрузили из памяти, пока оно было в фоне.
 */
class AppLockViewModel : ViewModel() {

    /** Замок пройден; `false` — при заданном PIN нужно показать замок. */
    var unlocked: Boolean by mutableStateOf(false)
        private set

    /** Замок пройден — или PIN только что создан в настройках: его ввели, спрашивать незачем. */
    fun unlock() {
        unlocked = true
    }

    /** Приложение ушло в фон: при возвращении замок снова нужен. */
    fun lock() {
        unlocked = false
    }
}
