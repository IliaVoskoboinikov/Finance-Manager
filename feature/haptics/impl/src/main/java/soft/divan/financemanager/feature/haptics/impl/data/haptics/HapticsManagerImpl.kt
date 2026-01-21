package soft.divan.financemanager.feature.haptics.impl.data.haptics

import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.haptics.impl.di.ApplicationScope
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
        if (!enabled) return
        if (!vibrator.hasVibrator()) return

        performInternal(type)
    }

    private fun performInternal(type: HapticType) {
        val effect = when (type) {
            HapticType.CLICK ->
                VibrationEffect.createOneShot(30, 50)

            HapticType.TOGGLE ->
                VibrationEffect.createOneShot(40, 80)

            HapticType.SUCCESS ->
                VibrationEffect.createWaveform(
                    longArrayOf(0, 30, 40, 50),
                    intArrayOf(0, 60, 0, 120),
                    -1
                )

            HapticType.ERROR ->
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 40, 60),
                    intArrayOf(0, 150, 0, 150),
                    -1
                )

            HapticType.LONG_PRESS ->
                VibrationEffect.createOneShot(70, 100)
        }

        vibrator.vibrate(effect)
    }
}