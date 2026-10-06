package soft.divan.financemanager.presenter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppLockViewModelTest {

    @Test
    fun `a new process starts locked`() {
        // Новый экземпляр — новый процесс: сохранённое состояние Activity сюда не попадает
        assertThat(AppLockViewModel().unlocked).isFalse()
    }

    @Test
    fun `unlock lasts until the app goes to the background`() {
        val lock = AppLockViewModel()

        lock.unlock()
        assertThat(lock.unlocked).isTrue()

        lock.lock()
        assertThat(lock.unlocked).isFalse()
    }
}
