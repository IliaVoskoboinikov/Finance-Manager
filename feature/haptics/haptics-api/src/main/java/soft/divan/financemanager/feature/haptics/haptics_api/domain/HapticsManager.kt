package soft.divan.financemanager.feature.haptics.haptics_api.domain

interface HapticsManager {
    fun perform(type: HapticType)
}