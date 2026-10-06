package soft.divan.financemanager.feature.security.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.R

class SecurityUiStateTest {

    @Test
    fun `new pin prompts ask to come up with a pin`() {
        assertThat(PinPrompt(PinPurpose.EnablePinLevel, create = true).titleRes)
            .isEqualTo(R.string.сome_up_with_pin)
        assertThat(PinPrompt(PinPurpose.ChangePinNew("2468"), create = true).titleRes)
            .isEqualTo(R.string.come_up_with_new_pin)
    }

    @Test
    fun `confirmation prompts ask for the current pin`() {
        listOf(
            PinPurpose.EnablePinLevel,
            PinPurpose.LeavePinLevel(DataProtectionLevel.DEVICE),
            PinPurpose.ChangePinCurrent,
            PinPurpose.DeletePin,
            PinPurpose.EnableBiometric
        ).forEach { purpose ->
            assertThat(PinPrompt(purpose).titleRes).isEqualTo(R.string.enter_current_pin)
        }
    }

    @Test
    fun `new pin is not exposed by toString`() {
        // Намерение PIN хранится в состоянии — оно не должно утекать в логи через data class
        assertThat(PinPurpose.ChangePinNew("2468").toString()).doesNotContain("2468")
    }

    @Test
    fun `every message has its text`() {
        assertThat(SecurityMessage.entries.map { it.textRes }).doesNotHaveDuplicates()
        assertThat(SecurityMessage.WRONG_PIN.textRes).isEqualTo(R.string.wrong_pin)
    }

    @Test
    fun `flow state starts idle`() {
        val state = SecurityFlowState()

        assertThat(state.pinPrompt).isNull()
        assertThat(state.inProgress).isFalse()
        assertThat(state.message).isNull()
    }

    @Test
    fun `error carries message resource`() {
        assertThat(SecurityUiState.Error(messageRes = 7).messageRes).isEqualTo(7)
    }

    @Test
    fun `lock screen starts without errors`() {
        val state = PinLockUiState()

        assertThat(state.error).isNull()
        assertThat(state.inProgress).isFalse()
        assertThat(KeyLostUiState().showConfirm).isFalse()
    }
}
