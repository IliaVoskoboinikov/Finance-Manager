package soft.divan.financemanager.presenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import soft.divan.financemanager.core.domain.repository.INITIAL_DATA_GENERATION
import javax.inject.Inject

/**
 * Сессия корневого графа навигации: хранилище его ViewModel (отдельное от хранилища Activity) и
 * поколение данных, на которых сессия построена.
 *
 * Nav3 чистит ViewModel экрана, только когда его ключ уходит из стека. Пока граф скрыт замком,
 * ключи остаются в стеке, и ViewModel ждут разблокировки — так и задумано. Но если данные стёрли,
 * пока граф скрыт (выход по истёкшей сессии, «Забыли PIN», исчерпанные попытки), стек этого не
 * узнает: без своего хранилища ViewModel прошлой сессии остались бы в хранилище Activity и после
 * разблокировки вернулись бы по тем же ключам — с данными прошлого пользователя. Поэтому сессия
 * помнит поколение данных ([onShown]) и становится устаревшей ([isStale]), когда оно сменилось.
 *
 * Как и хранилище Activity, переживает смену конфигурации, но не смерть процесса.
 *
 * Фабрики по умолчанию у владельца нет: ViewModel графа берутся только внутри entry, где
 * владельца с фабрикой и `SavedStateHandle` подставляет `rememberViewModelStoreNavEntryDecorator`.
 * `hiltViewModel()` прямо на уровне корневого графа, вне entry, с ним не работает.
 */
@HiltViewModel
class MainGraphViewModel @Inject constructor() : ViewModel(), ViewModelStoreOwner {

    override val viewModelStore: ViewModelStore = ViewModelStore()

    // Снимок Compose: после сброса хост перерисуется и построит граф заново. Начальное значение —
    // первое поколение процесса: сохранённый в Bundle стек прошлого процесса относится к данным,
    // какими они были на старте, и устаревает от первого же стирания, даже до первого показа
    private var sessionGeneration: Long by mutableLongStateOf(INITIAL_DATA_GENERATION)
    private var shown = false

    /**
     * Сессия построена на данных, которых уже нет, и показывать её нельзя, пока не [discard].
     *
     * Исключение — граф, который на экране и в этом кадре на нём остаётся ([staysShown]): его
     * сессию не сбросить, не выкинув пользователя с экрана. Выход из аккаунта там уводит стек на
     * авторизацию, а ViewModel снятых экранов чистит Nav3; очистка без выхода (гость отказался
     * объединять данные) стеки и ViewModel не трогает — экраны со списками переподпишутся на
     * новую базу, загруженные один раз сущности останутся как были.
     *
     * Граф, который в этом кадре скрывается, уже не исключение: стирание могло прийти, пока
     * Activity была в фоне и не перерисовывалась, — сессию нужно сбросить сразу, а не после
     * разблокировки.
     *
     * @param staysShown хост показывает граф в этом кадре.
     */
    fun isStale(generation: Long, staysShown: Boolean): Boolean =
        sessionGeneration != generation && !(staysShown && shown)

    /** Граф на экране и построен на данных поколения [generation]. */
    fun onShown(generation: Long) {
        shown = true
        sessionGeneration = generation
    }

    /** Граф скрыт — замком, экраном потери ключа или пустым фоном. */
    fun onHidden() {
        shown = false
    }

    /**
     * Закрывает сессию графа: все его ViewModel получают `onCleared`. Хранилище остаётся тем же
     * объектом и пустым — следующий показ графа создаст ViewModel заново, уже на данных поколения
     * [generation].
     */
    fun discard(generation: Long) {
        viewModelStore.clear()
        sessionGeneration = generation
    }

    override fun onCleared() {
        viewModelStore.clear()
    }
}
