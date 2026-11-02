package soft.divan.financemanager.presenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Прошёл ли пользователь замок приложения (PIN или отпечаток) в этом процессе и задан ли PIN.
 *
 * Состояние живёт в ViewModel, а не в `rememberSaveable`: оно переживает пересоздание Activity
 * (смена темы, масштаба шрифта), но не смерть процесса. Сохранённое состояние Activity система
 * восстанавливает и в новом процессе — с ним замок пропускался бы после того, как приложение
 * выгрузили из памяти, пока оно было в фоне.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor() : ViewModel() {

    /** Замок пройден; `false` — при заданном PIN нужно показать замок. */
    var unlocked: Boolean by mutableStateOf(false)
        private set

    /**
     * Задан ли PIN; `null` — ещё не прочитан. Пока так, главный экран не строится: иначе он
     * мелькал бы до замка.
     */
    var pinSet: Boolean? by mutableStateOf(null)
        private set

    /** Замок пройден. */
    fun unlock() {
        unlocked = true
    }

    /** Приложение ушло в фон: при возвращении замок снова нужен. */
    fun lock() {
        unlocked = false
    }

    /**
     * Новое значение «PIN задан». Только переход «точно не было → есть» — это PIN, созданный в
     * настройках при открытом приложении: его только что ввели дважды, и замок не запирает
     * экран. Первое прочтение (`null` → `true`) — холодный старт, там замок обязателен.
     */
    fun onPinSetChanged(set: Boolean) {
        if (pinSet == false && set) unlocked = true
        pinSet = set
    }
}
