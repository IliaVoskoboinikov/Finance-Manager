package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.security.keyset.KeyLevel

/** Уровень хранения ключа → уровень защиты, который видит пользователь. */
fun KeyLevel.toDomain(): DataProtectionLevel = when (this) {
    KeyLevel.OPEN -> DataProtectionLevel.NONE
    KeyLevel.DEVICE -> DataProtectionLevel.DEVICE
    KeyLevel.PIN -> DataProtectionLevel.PIN
}

/** Уровень защиты → как хранить ключ базы. */
fun DataProtectionLevel.toKeyLevel(): KeyLevel = when (this) {
    DataProtectionLevel.NONE -> KeyLevel.OPEN
    DataProtectionLevel.DEVICE -> KeyLevel.DEVICE
    DataProtectionLevel.PIN -> KeyLevel.PIN
}
