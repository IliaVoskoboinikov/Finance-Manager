package soft.divan.financemanager.presenter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.LocalDataState

class LocalDataScreenTest {

    @Test
    fun `lost key wins over everything`() {
        assertThat(resolveLocalDataScreen(LocalDataState.KeyLost, pinSet = true, unlocked = true))
            .isEqualTo(LocalDataScreen.KEY_LOST)
        assertThat(resolveLocalDataScreen(LocalDataState.KeyLost, pinSet = null, unlocked = false))
            .isEqualTo(LocalDataScreen.KEY_LOST)
    }

    @Test
    fun `locked database shows the data lock even when the ui lock is passed`() {
        val locked = LocalDataState.Locked(biometricEnabled = false)

        assertThat(resolveLocalDataScreen(locked, pinSet = true, unlocked = true))
            .isEqualTo(LocalDataScreen.DATA_LOCK)
        assertThat(resolveLocalDataScreen(locked, pinSet = null, unlocked = false))
            .isEqualTo(LocalDataScreen.DATA_LOCK)
    }

    @Test
    fun `nothing is built until keys and the pin are read`() {
        assertThat(resolveLocalDataScreen(LocalDataState.Initializing, pinSet = false, unlocked = true))
            .isEqualTo(LocalDataScreen.NOTHING)
        assertThat(resolveLocalDataScreen(LocalDataState.Open, pinSet = null, unlocked = true))
            .isEqualTo(LocalDataScreen.NOTHING)
    }

    @Test
    fun `open data with a pin not entered in this session shows the ui lock`() {
        assertThat(resolveLocalDataScreen(LocalDataState.Open, pinSet = true, unlocked = false))
            .isEqualTo(LocalDataScreen.UI_LOCK)
    }

    @Test
    fun `main screen needs open data and a passed or absent lock`() {
        assertThat(resolveLocalDataScreen(LocalDataState.Open, pinSet = true, unlocked = true))
            .isEqualTo(LocalDataScreen.MAIN)
        assertThat(resolveLocalDataScreen(LocalDataState.Open, pinSet = false, unlocked = false))
            .isEqualTo(LocalDataScreen.MAIN)
    }
}
