package soft.divan.financemanager.feature.haptic.haptic_api.domain

interface HapticManager {
    fun perform(type: HapticType)
}