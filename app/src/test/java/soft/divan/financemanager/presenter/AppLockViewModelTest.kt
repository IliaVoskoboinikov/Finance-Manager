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

    @Test
    fun `first read of an existing pin on a cold start keeps the lock`() {
        val lock = AppLockViewModel()

        lock.onPinSetChanged(true)

        assertThat(lock.pinSet).isTrue()
        assertThat(lock.unlocked).isFalse()
    }

    @Test
    fun `pin created in settings while the app is open does not lock it`() {
        val lock = AppLockViewModel()
        lock.onPinSetChanged(false)

        lock.onPinSetChanged(true)

        assertThat(lock.unlocked).isTrue()
    }

    @Test
    fun `pin removed by a wipe is reported and does not unlock anything`() {
        val lock = AppLockViewModel()
        lock.onPinSetChanged(true)

        lock.onPinSetChanged(false)

        assertThat(lock.pinSet).isFalse()
        assertThat(lock.unlocked).isFalse()
    }

    @Test
    fun `the same pin state read again changes nothing`() {
        val lock = AppLockViewModel()
        lock.onPinSetChanged(true)

        lock.onPinSetChanged(true)

        assertThat(lock.unlocked).isFalse()
    }
}
