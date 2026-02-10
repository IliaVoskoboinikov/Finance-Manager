package soft.divan.financemanager.feature.haptics.impl.data.haptics

import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase
import javax.inject.Inject

class HapticsManagerImpl @Inject constructor(
    private val vibrator: Vibrator,
    observeHapticEnabled: ObserveHapticsEnabledUseCase,
    @ApplicationScope scope: CoroutineScope
) : HapticsManager {

    @Volatile
    private var enabled: Boolean = true

    init {
        observeHapticEnabled()
            .onEach { enabled = it }
            .launchIn(scope)
    }

    override fun perform(type: HapticType) {
        if (!enabled || !vibrator.hasVibrator()) return
        performInternal(type)
    }

    private fun performInternal(type: HapticType) {
        val effect = when (type) {
            HapticType.CLICK ->
                VibrationEffect.createOneShot(CLICK_DURATION_MS, CLICK_AMPLITUDE)

            HapticType.TOGGLE ->
                VibrationEffect.createOneShot(TOGGLE_DURATION_MS, TOGGLE_AMPLITUDE)

            HapticType.SUCCESS ->
                VibrationEffect.createWaveform(SUCCESS_TIMINGS, SUCCESS_AMPLITUDES, NO_REPEAT)

            HapticType.ERROR ->
                VibrationEffect.createWaveform(ERROR_TIMINGS, ERROR_AMPLITUDES, NO_REPEAT)

            HapticType.LONG_PRESS ->
                VibrationEffect.createOneShot(LONG_PRESS_DURATION_MS, LONG_PRESS_AMPLITUDE)
        }

        vibrator.vibrate(effect)
    }

    private companion object {

        const val NO_REPEAT = -1

        // CLICK
        const val CLICK_DURATION_MS = 30L
        const val CLICK_AMPLITUDE = 50

        // TOGGLE
        const val TOGGLE_DURATION_MS = 40L
        const val TOGGLE_AMPLITUDE = 80

        // LONG PRESS
        const val LONG_PRESS_DURATION_MS = 70L
        const val LONG_PRESS_AMPLITUDE = 100

        // SUCCESS
        val SUCCESS_TIMINGS = longArrayOf(0, 30, 40, 50)
        val SUCCESS_AMPLITUDES = intArrayOf(0, 60, 0, 120)

        // ERROR
        val ERROR_TIMINGS = longArrayOf(0, 60, 40, 60)
        val ERROR_AMPLITUDES = intArrayOf(0, 150, 0, 150)
    }
}
// Revue me>>
