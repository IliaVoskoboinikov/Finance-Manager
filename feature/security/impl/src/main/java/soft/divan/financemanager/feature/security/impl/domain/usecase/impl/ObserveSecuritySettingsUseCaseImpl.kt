package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecuritySettingsUseCase
import javax.inject.Inject

class ObserveSecuritySettingsUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository,
    private val securityRepository: SecurityRepository,
    private val biometrics: LocalDataBiometrics,
    private val secureScreenRepository: SecureScreenRepository,
    private val getAuthStatus: GetAuthStatusUseCase
) : ObserveSecuritySettingsUseCase {

    override fun invoke(): Flow<SecuritySettings> = combine(
        protection.observeLevel(),
        securityRepository.observePinSet(),
        biometrics.observeEnabled(),
        secureScreenRepository.observeEnabled(),
        getAuthStatus()
    ) { level, hasPin, biometric, secureScreen, status ->
        SecuritySettings(
            level = level,
            hasPin = hasPin,
            biometricEnabled = biometric,
            secureScreen = secureScreen,
            isGuest = status != AuthStatus.AUTHORIZED
        )
    }
}
